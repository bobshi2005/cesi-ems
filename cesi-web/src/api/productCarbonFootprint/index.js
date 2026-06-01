import request from '@/utils/request'

const BASE = '/pcf'

/* ─── 分类 & 产品 ─── */

export function getCategoryTree() {
  return request({ url: `${BASE}/category/tree`, method: 'get' })
}

export function saveCategory(data) {
  return request({ url: `${BASE}/category/save`, method: 'post', data })
}

export function removeCategory(id) {
  return request({ url: `${BASE}/category/${id}`, method: 'delete' })
}

export function listProductsByCategory(categoryId) {
  return request({ url: `${BASE}/category/products`, method: 'get', params: { categoryId } })
}

export function saveProduct(data) {
  return request({ url: `${BASE}/category/product/save`, method: 'post', data })
}

export function removeProduct(id) {
  return request({ url: `${BASE}/category/product/${id}`, method: 'delete' })
}

/* ─── 生命周期总览 ─── */

export function getLifecycleOverview(productId, year) {
  return request({ url: `${BASE}/footprint/lifecycle`, method: 'get', params: { productId, year } })
}

/* ─── 原材料 ─── */

export function listRawMaterial(productId, year) {
  return request({ url: `${BASE}/footprint/raw/list`, method: 'get', params: { productId, year } })
}
export function saveRawMaterial(data) {
  return request({ url: `${BASE}/footprint/raw/save`, method: 'post', data })
}
export function removeRawMaterial(id) {
  return request({ url: `${BASE}/footprint/raw/${id}`, method: 'delete' })
}

/* ─── 生产制造 ─── */

export function listManufacturing(productId, year) {
  return request({ url: `${BASE}/footprint/mfg/list`, method: 'get', params: { productId, year } })
}
export function saveManufacturing(data) {
  return request({ url: `${BASE}/footprint/mfg/save`, method: 'post', data })
}
export function removeManufacturing(id) {
  return request({ url: `${BASE}/footprint/mfg/${id}`, method: 'delete' })
}

/* ─── 物流运输 ─── */

export function listLogistics(productId, year) {
  return request({ url: `${BASE}/footprint/logistics/list`, method: 'get', params: { productId, year } })
}
export function saveLogistics(data) {
  return request({ url: `${BASE}/footprint/logistics/save`, method: 'post', data })
}
export function removeLogistics(id) {
  return request({ url: `${BASE}/footprint/logistics/${id}`, method: 'delete' })
}

/* ─── 产品使用 ─── */

export function listProductUse(productId, year) {
  return request({ url: `${BASE}/footprint/use/list`, method: 'get', params: { productId, year } })
}
export function saveProductUse(data) {
  return request({ url: `${BASE}/footprint/use/save`, method: 'post', data })
}
export function removeProductUse(id) {
  return request({ url: `${BASE}/footprint/use/${id}`, method: 'delete' })
}

/* ─── 废弃回收 ─── */

export function listEol(productId, year) {
  return request({ url: `${BASE}/footprint/eol/list`, method: 'get', params: { productId, year } })
}
export function saveEol(data) {
  return request({ url: `${BASE}/footprint/eol/save`, method: 'post', data })
}
export function removeEol(id) {
  return request({ url: `${BASE}/footprint/eol/${id}`, method: 'delete' })
}

/* ─── 绿电绿证 ─── */

export function listGreenCert(productId, year) {
  return request({ url: `${BASE}/footprint/cert/list`, method: 'get', params: { productId, year } })
}
export function saveGreenCert(data) {
  return request({ url: `${BASE}/footprint/cert/save`, method: 'post', data })
}
export function removeGreenCert(id) {
  return request({ url: `${BASE}/footprint/cert/${id}`, method: 'delete' })
}

/* ─── 配置：排放因子 ─── */

export function listEmissionFactors(factorCategory) {
  return request({ url: `${BASE}/config/factor/list`, method: 'get', params: { factorCategory } })
}
export function saveEmissionFactor(data) {
  return request({ url: `${BASE}/config/factor/save`, method: 'post', data })
}
export function removeEmissionFactor(id) {
  return request({ url: `${BASE}/config/factor/${id}`, method: 'delete' })
}

/* ─── 配置：核算参数 ─── */

export function getAccountingParams() {
  return request({ url: `${BASE}/config/params`, method: 'get' })
}
export function saveAccountingParams(data) {
  return request({ url: `${BASE}/config/params/save`, method: 'post', data })
}

/* ─── 配置：绿证系数 ─── */

export function listGreenCertFactors() {
  return request({ url: `${BASE}/config/greenFactor/list`, method: 'get' })
}
export function saveGreenCertFactor(data) {
  return request({ url: `${BASE}/config/greenFactor/save`, method: 'post', data })
}
export function removeGreenCertFactor(id) {
  return request({ url: `${BASE}/config/greenFactor/${id}`, method: 'delete' })
}
