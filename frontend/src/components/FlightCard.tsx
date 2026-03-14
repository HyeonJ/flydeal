interface FlightOffer {
  offerId: string
  totalAmount: string
  totalCurrency: string
  airline: string
  departureTime: string
  arrivalTime: string
  origin: string
  destination: string
  stops: number
}

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

export function FlightCard({ offer }: FlightCardProps) {
  return (
    <div className="flight-card">
      <div className="flight-card-header">
        <div className="airline-name">{offer.airline || '항공사'}</div>
        <div className="price">
          <span className="price-amount">${Number(offer.totalAmount).toLocaleString()}</span>
          <span className="price-currency"> {offer.totalCurrency}</span>
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
