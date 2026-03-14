import type { FlightSearchParams, FlightSearchResponse } from '../types/flight'

export async function searchFlights(params: FlightSearchParams): Promise<FlightSearchResponse> {
  const query = new URLSearchParams({
    origin: params.origin.toUpperCase(),
    destination: params.destination.toUpperCase(),
    departureDate: params.departureDate,
    ...(params.returnDate ? { returnDate: params.returnDate } : {}),
  })

  const response = await fetch(`/api/search/flights?${query.toString()}`)

  if (!response.ok) {
    const errorText = await response.text()
    throw new Error(`검색 실패 (${response.status}): ${errorText}`)
  }

  return response.json()
}
