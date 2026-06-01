import request from '@/utils/request'

export function getCarbonBudgetPage(params) {
  return request({ url: '/budget/carbon/page', method: 'get', params })
}

export function addCarbonBudget(data) {
  return request({ url: '/budget/carbon/add', method: 'post', data })
}

export function editCarbonBudget(data) {
  return request({ url: '/budget/carbon/edit', method: 'put', data })
}

export function deleteCarbonBudget(id) {
  return request({ url: `/budget/carbon/${id}`, method: 'delete' })
}

export function deleteCarbonBudgetBatch(ids) {
  return request({ url: '/budget/carbon/batch', method: 'delete', data: ids })
}
