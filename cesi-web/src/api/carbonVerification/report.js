import request from '@/utils/request'

export function getReportPage(params) {
  return request({ url: '/carbonVerification/report/page', method: 'get', params })
}

export function checkReportStatus(year, orgUnit = '全厂') {
  return request({ url: '/carbonVerification/report/checkStatus', method: 'get', params: { year, orgUnit } })
}

export function generateReport(year, orgUnit = '全厂', standard, templateType, sections) {
  return request({
    url: '/carbonVerification/report/generate',
    method: 'post',
    params: { year, orgUnit, standard, templateType, sections }
  })
}

export function previewReport(id) {
  return request({ url: `/carbonVerification/report/preview/${id}`, method: 'get' })
}

export function exportReport(id) {
  return request({ url: `/carbonVerification/report/export/${id}`, method: 'get', responseType: 'blob' })
}

export function deleteReport(id) {
  return request({ url: `/carbonVerification/report/${id}`, method: 'delete' })
}
