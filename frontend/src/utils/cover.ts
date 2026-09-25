export function coverStyle(url?: string) {
  if (!url || url.length > 500 || /[\s"'()<>\\]/.test(url)) {
    return {}
  }
  if (!url.startsWith('https://') && !url.startsWith('/files/')) {
    return {}
  }
  return { backgroundImage: `url("${url}")` }
}

const articleGradients = [
  'linear-gradient(135deg, #e6ded4, #cbb9a5)',
  'linear-gradient(135deg, #efe8df, #d3c2ad)',
  'linear-gradient(135deg, #f2ece5, #dccfbe)'
]

// 文章没有封面图时用渐变色兜底，按序号错开
export function articleStyle(url: string | undefined, index = 0) {
  const style = coverStyle(url)
  if (Object.keys(style).length > 0) {
    return style
  }
  return { background: articleGradients[index % articleGradients.length] }
}
