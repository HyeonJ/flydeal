import { useState } from 'react'
import { SearchForm } from './components/SearchForm'
import { FlightResults } from './components/FlightResults'
import { searchFlights } from './api/searchApi'
import type { FlightOffer, FlightSearchRequest, FlightSearchResult } from './types/flight'
import './index.css'

function App() {
  const [result, setResult] = useState<FlightSearchResult | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [searched, setSearched] = useState(false)

  const handleSearch = async (request: FlightSearchRequest) => {
    setLoading(true)
    setError(null)
    setSearched(true)

    try {
      const searchResult = await searchFlights(request)
      setResult(searchResult)
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.')
      setResult(null)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1 className="logo">FlyDeal</h1>
        <p className="tagline">항공편 최저가를 찾아보세요</p>
      </header>

      <main className="app-main">
        <SearchForm onSearch={handleSearch} loading={loading} />
        <FlightResults
          result={result}
          loading={loading}
          error={error}
          searched={searched}
        />
      </main>
    </div>
  )
}

export default App
