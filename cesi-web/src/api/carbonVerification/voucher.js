import request from '@/utils/request'

export function getVoucherPage(params) {
  return request({ url: '/carbonVerification/voucher/page', method: 'get', params })
}

export function uploadVoucher(formData) {
  return request({
    url: '/carbonVerification/voucher/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function previewVoucherUrl(id) {
  // 返回预览 URL，前端用 window.open 打开
  const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
  return `/dev-api/carbonVerification/voucher/preview/${id}?Authorization=Bearer ${encodeURIComponent(token)}`
}

export function downloadVoucher(id) {
  return request({ url: `/carbonVerification/voucher/download/${id}`, method: 'get', responseType: 'blob' })
}

export function batchDownloadVoucher(ids) {
  return request({ url: '/carbonVerification/voucher/batchDownload', method: 'post', data: ids, responseType: 'blob' })
}

export function deleteVoucher(id) {
  return request({ url: `/carbonVerification/voucher/${id}`, method: 'delete' })
}

export function exportVoucherList(year, orgUnit = '全厂') {
  return request({ url: '/carbonVerification/voucher/exportList', method: 'post', params: { year, orgUnit }, responseType: 'blob' })
}

export function getVoucherBySourceMonth(year, month, emissionSource, orgUnit = '全厂') {
  return request({
    url: '/carbonVerification/voucher/page',
    method: 'get',
    params: { year, month, emissionSource, orgUnit, pageNum: 1, pageSize: 50 }
  })
}
