import request from '@/utils/request'

export function getCarbonAssetData(year) {
  return request({
    url: '/carbonAsset/data',
    method: 'get',
    params: { year }
  })
}

export function saveCarbonAsset(data) {
  return request({
    url: '/carbonAsset/save',
    method: 'post',
    data
  })
}
