import { useState } from 'react'
import { SearchForm } from './components/SearchForm'
import { FlightResults } from './components/FlightResults'
import { searchFlights } from './api/searchApi'
import type { FlightOffer, FlightSearchParams } from './types/flight'
import './index.css'

function App() {
  const [offers, setOffers] = useState<FlightOffer[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [searched, setSearched] = useState(false)

  const handleSearch = async (params: FlightSearchParams) => {
    setLoading(true)
    setError(null)
    setSearched(true)

    try {
      const result = await searchFlights(params)
      setOffers((result as any).offers ?? result.data ?? [])
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.')
      setOffers([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1 className="logo">✈ FlyDeal</h1>
        <p className="tagline">항공권 최저가 비교</p>
      </header>

      <main className="app-main">
        <SearchForm onSearch={handleSearch} loading={loading} />
        <FlightResults
          offers={offers}
          loading={loading}
          error={error}
          searched={searched}
        />
      </main>
    </div>
  )
}

export default App
