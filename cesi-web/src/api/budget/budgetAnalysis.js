import request from '@/utils/request'

export function getBudgetAnalysis(params) {
  return request({ url: '/budget/analysis', method: 'get', params })
}
