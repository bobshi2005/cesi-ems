import request from '@/utils/request'

export function getSupplyChainCarbonPage(params) {
  return request({
    url: '/supplyChainCarbon/page',
    method: 'get',
    params
  })
}

export function addSupplyChainCarbon(data) {
  return request({
    url: '/supplyChainCarbon/add',
    method: 'post',
    data
  })
}

export function editSupplyChainCarbon(data) {
  return request({
    url: '/supplyChainCarbon/edit',
    method: 'put',
    data
  })
}

export function deleteSupplyChainCarbon(id) {
  return request({
    url: `/supplyChainCarbon/${id}`,
    method: 'delete'
  })
}
