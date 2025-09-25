<template>
  <svg class="svg-icon" aria-hidden="true">
    <use :xlink:href="`#icon-${iconClass}`"></use>
  </svg>
</template>

<script setup>
defineProps({
  iconClass: {
    type: String,
    required: true
  }
})
</script>

<style scoped>
.svg-icon {
  width: 1em;
  height: 1em;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
}
</style>