export interface FlightOffer {
  id: string
  source: 'DUFFEL' | 'KIWI'
  airline: string
  airlineLogo: string | null
  departureTime: string
  arrivalTime: string
  origin: string
  destination: string
  durationMinutes: number
  stops: number
  price: number
  currency: string
  deepLink: string | null
}

export interface FlightSearchRequest {
  origin: string
  destination: string
  departureDate: string
  returnDate?: string
  flightType: 'round' | 'oneway'
  passengers: number
  cabinClass: 'economy' | 'business'
}

export interface FlightSearchResult {
  offers: FlightOffer[]
  sources: string[]
  cached: boolean
  totalCount: number
  warnings?: string[]
}

export interface ApiResponse<T> {
  success: boolean
  data?: T
  message?: string
}
