import { useState, FormEvent } from 'react'
import type { FlightSearchParams } from '../types/flight'

interface SearchFormProps {
  onSearch: (params: FlightSearchParams) => void
  loading: boolean
}

export function SearchForm({ onSearch, loading }: SearchFormProps) {
  const [origin, setOrigin] = useState('')
  const [destination, setDestination] = useState('')
  const [departureDate, setDepartureDate] = useState('')
  const [returnDate, setReturnDate] = useState('')

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (!origin || !destination || !departureDate) return
    onSearch({
      origin,
      destination,
      departureDate,
      returnDate: returnDate || undefined,
    })
  }

  return (
    <form className="search-form" onSubmit={handleSubmit}>
      <div className="form-row">
        <div className="form-group">
          <label htmlFor="origin">출발 공항</label>
          <input
            id="origin"
            type="text"
            placeholder="예: ICN"
            value={origin}
            onChange={(e) => setOrigin(e.target.value.toUpperCase())}
            maxLength={3}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="destination">도착 공항</label>
          <input
            id="destination"
            type="text"
            placeholder="예: NRT"
            value={destination}
            onChange={(e) => setDestination(e.target.value.toUpperCase())}
            maxLength={3}
            required
          />
        </div>

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

        <div className="form-group">
          <label htmlFor="returnDate">귀국일 (선택)</label>
          <input
            id="returnDate"
            type="date"
            value={returnDate}
            onChange={(e) => setReturnDate(e.target.value)}
            min={departureDate}
          />
        </div>
      </div>

      <button type="submit" className="search-btn" disabled={loading}>
        {loading ? '검색 중...' : '항공편 검색'}
      </button>
    </form>
  )
}
