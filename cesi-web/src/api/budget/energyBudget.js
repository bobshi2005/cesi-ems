import request from '@/utils/request'

export function getEnergyBudgetPage(params) {
  return request({ url: '/budget/energy/page', method: 'get', params })
}

export function addEnergyBudget(data) {
  return request({ url: '/budget/energy/add', method: 'post', data })
}

export function editEnergyBudget(data) {
  return request({ url: '/budget/energy/edit', method: 'put', data })
}

export function deleteEnergyBudget(id) {
  return request({ url: `/budget/energy/${id}`, method: 'delete' })
}

export function deleteEnergyBudgetBatch(ids) {
  return request({ url: '/budget/energy/batch', method: 'delete', data: ids })
}
