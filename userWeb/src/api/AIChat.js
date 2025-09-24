export async function chatWithAI({ message, stream = true, endpoint, token, onChunk }) {
    const controller = new AbortController();
    const requestBody = {
        model: 'qwen3:8b',
        messages: [
            { role: 'system', content: '你是一个乐于助人的AI助手。' },
            { role: 'user', content: message }
        ],
        stream
    };

    if (stream) {
        try {
            const response = await fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                    'Accept': 'text/event-stream'
                },
                body: JSON.stringify(requestBody),
                signal: controller.signal
            });

            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            let buffer = '';

            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                buffer += decoder.decode(value, { stream: true });
                const lines = buffer.split('\n');
                buffer = lines.pop() || '';

                for (const line of lines) {
                    if (line.startsWith('data:')) {
                        const dataContent = line.slice(5).trim();
                        if (dataContent === '[DONE]') return;
                        try {
                            const chunk = JSON.parse(dataContent);
                            onChunk?.(chunk);
                        } catch (e) {
                            console.warn('JSON解析失败:', e);
                        }
                    }
                }
            }
        } catch (err) {
            if (err.name !== 'AbortError') throw err;
        }
    } else {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const data = await response.json();
        onChunk?.(data);
    }

    return controller; // 可用于中断
}
