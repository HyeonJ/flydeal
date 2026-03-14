export interface FlightSegment {
  departureAirport: string
  arrivalAirport: string
  departureTime: string
  arrivalTime: string
  carrierCode: string
  flightNumber: string
  duration: string
}

export interface FlightItinerary {
  segments: FlightSegment[]
  duration: string
}

export interface FlightPrice {
  totalAmount: string
  currency: string
}

export interface FlightOffer {
  id: string
  airline: string
  price: FlightPrice
  itineraries: FlightItinerary[]
  numberOfStops: number
  validatingAirlineCodes?: string[]
}

export interface FlightSearchParams {
  origin: string
  destination: string
  departureDate: string
  returnDate?: string
}

export interface FlightSearchResponse {
  data: FlightOffer[]
  meta?: {
    count: number
  }
}
