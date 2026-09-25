export function coverStyle(url?: string) {
  if (!url || url.length > 500 || /[\s"'()<>\\]/.test(url)) {
    return {}
  }
  if (!url.startsWith('https://') && !url.startsWith('/files/')) {
    return {}
  }
  return { backgroundImage: `url("${url}")` }
}
