import type { FlightOffer } from '../types/flight'
import { FlightCard } from './FlightCard'

interface FlightResultsProps {
  offers: FlightOffer[]
  loading: boolean
  error: string | null
  searched: boolean
}

export function FlightResults({ offers, loading, error, searched }: FlightResultsProps) {
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

  if (!searched) {
    return (
      <div className="results-state placeholder">
        <p>출발지, 목적지, 날짜를 입력하고 검색해보세요.</p>
      </div>
    )
  }

  if (offers.length === 0) {
    return (
      <div className="results-state">
        <p>검색 결과가 없습니다.</p>
      </div>
    )
  }

  return (
    <div className="results-list">
      <p className="results-count">{offers.length}개의 항공편이 검색됐어요</p>
      {offers.map((offer) => (
        <FlightCard key={offer.id} offer={offer} />
      ))}
    </div>
  )
}
