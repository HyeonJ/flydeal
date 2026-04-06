import { useState, FormEvent } from 'react'
import type { FlightSearchRequest } from '../types/flight'

interface SearchFormProps {
  onSearch: (request: FlightSearchRequest) => void
  loading: boolean
}

export function SearchForm({ onSearch, loading }: SearchFormProps) {
  const [origin, setOrigin] = useState('')
  const [destination, setDestination] = useState('')
  const [departureDate, setDepartureDate] = useState('')
  const [returnDate, setReturnDate] = useState('')
  const [flightType, setFlightType] = useState<'round' | 'oneway'>('round')
  const [passengers, setPassengers] = useState(1)
  const [cabinClass, setCabinClass] = useState<'economy' | 'business'>('economy')

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (!origin || !destination || !departureDate) return

    onSearch({
      origin: origin.toUpperCase(),
      destination: destination.toUpperCase(),
      departureDate,
      returnDate: flightType === 'round' && returnDate ? returnDate : undefined,
      flightType,
      passengers,
      cabinClass,
    })
  }

  return (
    <form className="search-form" onSubmit={handleSubmit}>
      <div className="form-row">
        <div className="flight-type-toggle">
          <button
            type="button"
            className={`toggle-btn ${flightType === 'round' ? 'active' : ''}`}
            onClick={() => setFlightType('round')}
          >
            왕복
          </button>
          <button
            type="button"
            className={`toggle-btn ${flightType === 'oneway' ? 'active' : ''}`}
            onClick={() => setFlightType('oneway')}
          >
            편도
          </button>
        </div>
      </div>

      <div className="form-row">
        <div className="form-group">
          <label htmlFor="origin">출발지</label>
          <input
            id="origin"
            type="text"
            placeholder="ICN"
            value={origin}
            onChange={(e) => setOrigin(e.target.value.toUpperCase())}
            maxLength={3}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="destination">도착지</label>
          <input
            id="destination"
            type="text"
            placeholder="NRT"
            value={destination}
            onChange={(e) => setDestination(e.target.value.toUpperCase())}
            maxLength={3}
            required
          />
        </div>
      </div>

      <div className="form-row">
        <div className="form-group">
          <label htmlFor="departureDate">출발일</label>
          <input
            id="departureDate"
            type="date"
            value={departureDate}
            onChange={(e) => setDepartureDate(e.target.value)}
            required
          />
        </div>

        {flightType === 'round' && (
          <div className="form-group">
            <label htmlFor="returnDate">귀국일</label>
            <input
              id="returnDate"
              type="date"
              value={returnDate}
              onChange={(e) => setReturnDate(e.target.value)}
              min={departureDate}
              required
            />
          </div>
        )}

        <div className="form-group">
          <label htmlFor="passengers">승객</label>
          <select
            id="passengers"
            value={passengers}
            onChange={(e) => setPassengers(Number(e.target.value))}
          >
            {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((n) => (
              <option key={n} value={n}>{n}명</option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="cabinClass">좌석 등급</label>
          <select
            id="cabinClass"
            value={cabinClass}
            onChange={(e) => setCabinClass(e.target.value as 'economy' | 'business')}
          >
            <option value="economy">이코노미</option>
            <option value="business">비즈니스</option>
          </select>
        </div>
      </div>

      <button type="submit" className="search-btn" disabled={loading}>
        {loading ? '검색 중...' : '항공편 검색'}
      </button>
    </form>
  )
}
