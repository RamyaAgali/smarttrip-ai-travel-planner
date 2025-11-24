export default function WeatherSummary({ weather }) {
  if (!weather) return null;

  return (
    <div className="bg-gradient-to-r from-blue-100 to-blue-200 p-4 rounded-xl shadow-md text-center mb-6">
      <h2 className="text-2xl font-bold text-blue-700 mb-2">🌤 Current Weather</h2>
      <p className="text-lg text-gray-800">
        <strong>Condition:</strong> {weather.condition}
      </p>
      <p className="text-gray-700">
        🌡 Temperature: <strong>{weather.temperature}°C</strong> | 💨 Wind Speed:{" "}
        <strong>{weather.windspeed} km/h</strong>
      </p>
    </div>
  );
}
