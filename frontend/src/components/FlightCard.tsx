import type { FlightOffer } from '../types/flight'

interface FlightCardProps {
  offer: FlightOffer
}

function formatTime(isoString: string): string {
  try {
    return new Date(isoString).toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    })
  } catch {
    return isoString
  }
}

function formatDate(isoString: string): string {
  try {
    return new Date(isoString).toLocaleDateString('ko-KR', {
      month: 'short',
      day: 'numeric',
    })
  } catch {
    return ''
  }
}

function formatDuration(minutes: number): string {
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return `${h}시간 ${m}분`
}

function formatPrice(price: number): string {
  return price.toLocaleString('ko-KR')
}

export function FlightCard({ offer }: FlightCardProps) {
  return (
    <div className="flight-card">
      <div className="flight-card-header">
        <div className="airline-info">
          {offer.airlineLogo && (
            <img src={offer.airlineLogo} alt={offer.airline} className="airline-logo" />
          )}
          <span className="airline-name">{offer.airline || '항공사'}</span>
        </div>
        <div className="price">
          <span className="price-amount">₩{formatPrice(offer.price)}</span>
          <span className="source-badge">{offer.source}</span>
        </div>
      </div>

      <div className="itinerary">
        <div className="segment-times">
          <div className="time-block">
            <div className="time">{formatTime(offer.departureTime)}</div>
            <div className="airport">{offer.origin}</div>
            <div className="date">{formatDate(offer.departureTime)}</div>
          </div>

          <div className="flight-path">
            <div className="duration">{formatDuration(offer.durationMinutes)}</div>
            <div className="path-line">
              <span className="dot" />
              <span className="line" />
              {offer.stops > 0 && <span className="stop-dot" />}
              {offer.stops > 0 && <span className="line" />}
              <span className="dot" />
            </div>
            <div className="stops">
              {offer.stops === 0 ? '직항' : `경유 ${offer.stops}회`}
            </div>
          </div>

          <div className="time-block">
            <div className="time">{formatTime(offer.arrivalTime)}</div>
            <div className="airport">{offer.destination}</div>
            <div className="date">{formatDate(offer.arrivalTime)}</div>
          </div>
        </div>
      </div>
    </div>
  )
}
