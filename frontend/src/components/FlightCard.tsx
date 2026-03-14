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

function formatDuration(duration: string): string {
  // Parse ISO 8601 duration like PT2H30M
  const match = duration.match(/PT(?:(\d+)H)?(?:(\d+)M)?/)
  if (!match) return duration
  const hours = match[1] ? `${match[1]}시간` : ''
  const minutes = match[2] ? ` ${match[2]}분` : ''
  return `${hours}${minutes}`.trim()
}

export function FlightCard({ offer }: FlightCardProps) {
  const outbound = offer.itineraries[0]
  const inbound = offer.itineraries[1]

  const firstSegment = outbound?.segments[0]
  const lastSegment = outbound?.segments[outbound.segments.length - 1]

  return (
    <div className="flight-card">
      <div className="flight-card-header">
        <div className="airline-name">{offer.airline || offer.validatingAirlineCodes?.[0] || '항공사'}</div>
        <div className="price">
          <span className="price-amount">{Number(offer.price.totalAmount).toLocaleString()}</span>
          <span className="price-currency"> {offer.price.currency}</span>
        </div>
      </div>

      <div className="itinerary">
        <div className="segment-times">
          <div className="time-block">
            <div className="time">{firstSegment ? formatTime(firstSegment.departureTime) : '-'}</div>
            <div className="airport">{firstSegment?.departureAirport}</div>
          </div>

          <div className="flight-path">
            <div className="duration">{outbound ? formatDuration(outbound.duration) : ''}</div>
            <div className="path-line">
              <span className="dot" />
              <span className="line" />
              {offer.numberOfStops > 0 && <span className="stop-dot" />}
              {offer.numberOfStops > 0 && <span className="line" />}
              <span className="dot" />
            </div>
            <div className="stops">
              {offer.numberOfStops === 0
                ? '직항'
                : `경유 ${offer.numberOfStops}회`}
            </div>
          </div>

          <div className="time-block">
            <div className="time">{lastSegment ? formatTime(lastSegment.arrivalTime) : '-'}</div>
            <div className="airport">{lastSegment?.arrivalAirport}</div>
          </div>
        </div>
      </div>

      {inbound && (() => {
        const inFirst = inbound.segments[0]
        const inLast = inbound.segments[inbound.segments.length - 1]
        return (
          <div className="itinerary itinerary-return">
            <div className="segment-times">
              <div className="time-block">
                <div className="time">{formatTime(inFirst.departureTime)}</div>
                <div className="airport">{inFirst.departureAirport}</div>
              </div>

              <div className="flight-path">
                <div className="duration">{formatDuration(inbound.duration)}</div>
                <div className="path-line">
                  <span className="dot" />
                  <span className="line" />
                  <span className="dot" />
                </div>
                <div className="stops">귀국편</div>
              </div>

              <div className="time-block">
                <div className="time">{formatTime(inLast.arrivalTime)}</div>
                <div className="airport">{inLast.arrivalAirport}</div>
              </div>
            </div>
          </div>
        )
      })()}
    </div>
  )
}
