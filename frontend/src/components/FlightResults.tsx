import { FlightCard } from './FlightCard'
import type { FlightSearchResult } from '../types/flight'

interface FlightResultsProps {
  result: FlightSearchResult | null
  loading: boolean
  error: string | null
  searched: boolean
}

export function FlightResults({ result, loading, error, searched }: FlightResultsProps) {
  if (loading) {
    return (
      <div className="results-state">
        <div className="spinner" />
        <p>항공편 검색 중...</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="results-state error">
        <p>{error}</p>
      </div>
    )
  }

  if (!searched || !result) {
    return (
      <div className="results-state placeholder">
        <p>출발지, 도착지, 날짜를 입력하고 검색해보세요.</p>
      </div>
    )
  }

  if (result.offers.length === 0) {
    return (
      <div className="results-state">
        <p>검색 결과가 없습니다. 다른 날짜나 목적지로 검색해보세요.</p>
      </div>
    )
  }

  return (
    <div className="results-list">
      {result.warnings && result.warnings.length > 0 && (
        <div className="results-warning">
          {result.warnings.map((w, i) => <p key={i}>{w}</p>)}
        </div>
      )}
      <p className="results-count">
        {result.totalCount}개의 항공편 · {result.sources.join(' + ')}
      </p>
      {result.offers.map((offer) => (
        <FlightCard key={offer.id} offer={offer} />
      ))}
    </div>
  )
}
