import request from '@/utils/request'

export function getActivitySourceList(year, orgUnit = '全厂') {
  return request({ url: '/carbonVerification/activity/sourceList', method: 'get', params: { year, orgUnit } })
}

export function getActivityPage(params) {
  return request({ url: '/carbonVerification/activity/page', method: 'get', params })
}

export function getActivitySummary(year, orgUnit = '全厂') {
  return request({ url: '/carbonVerification/activity/summary', method: 'get', params: { year, orgUnit } })
}

export function getActivityDetail(year, emissionSource, orgUnit = '全厂') {
  return request({ url: '/carbonVerification/activity/detail', method: 'get', params: { year, emissionSource, orgUnit } })
}

export function addActivity(data) {
  return request({ url: '/carbonVerification/activity/add', method: 'post', data })
}

export function editActivity(data) {
  return request({ url: '/carbonVerification/activity/edit', method: 'put', data })
}

export function deleteActivity(id) {
  return request({ url: `/carbonVerification/activity/${id}`, method: 'delete' })
}

export function importActivityTemplate() {
  return request({ url: '/carbonVerification/activity/importTemplate', method: 'get', responseType: 'blob' })
}

export function updateActivityFactor(data) {
  return request({ url: '/carbonVerification/activity/updateFactor', method: 'put', data })
}

export function exportActivity(year, orgUnit = '全厂') {
  return request({ url: '/carbonVerification/activity/export', method: 'post', params: { year, orgUnit }, responseType: 'blob' })
}
