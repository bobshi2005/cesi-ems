import request from '@/utils/request'

export function getPackagePage(params) {
  return request({ url: '/carbonVerification/package/page', method: 'get', params })
}

export function getPackageCompleteness(year, orgUnit = '全厂') {
  return request({ url: '/carbonVerification/package/completeness', method: 'get', params: { year, orgUnit } })
}

export function generatePackage(data) {
  return request({ url: '/carbonVerification/package/generate', method: 'post', data })
}

export function downloadPackage(id) {
  return request({ url: `/carbonVerification/package/download/${id}`, method: 'get', responseType: 'blob' })
}

export function deletePackage(id) {
  return request({ url: `/carbonVerification/package/${id}`, method: 'delete' })
}
