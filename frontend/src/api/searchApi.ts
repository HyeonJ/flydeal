import type { FlightSearchRequest, FlightSearchResult, ApiResponse } from '../types/flight'

export async function searchFlights(request: FlightSearchRequest): Promise<FlightSearchResult> {
  const response = await fetch('/api/flights/search', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null)
    const message = errorBody?.message ?? `검색 실패 (${response.status})`
    throw new Error(message)
  }

  const apiResponse: ApiResponse<FlightSearchResult> = await response.json()

  if (!apiResponse.success || !apiResponse.data) {
    throw new Error(apiResponse.message ?? '검색 결과를 불러올 수 없습니다.')
  }

  return apiResponse.data
}
