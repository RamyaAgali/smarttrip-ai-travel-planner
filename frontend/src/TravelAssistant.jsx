import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from "recharts";
import WeatherSummary from "./WeatherSummary";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";

export default function TravelAssistant() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
  from: "",           // ✅ NEW: optional starting city
  destination: "",
  days: 3,
  travelMode: "Flight",
  currency: "USD",
});
  const [plan, setPlan] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const COLORS = ["#0088FE", "#00C49F", "#FFBB28", "#FF8042"];
  const currencySymbols = {
    INR: "₹",
    USD: "$",
    EUR: "€",
    GBP: "£",
    JPY: "¥",
  };
    const generatePlan = async () => {
  if (!formData.destination.trim()) {
    setError("Please enter a destination!");
    return;
  }

  setError("");
  setLoading(true);
  setPlan(null);

  try {
    // 🧭 Build query params
    const query = new URLSearchParams({
      destination: formData.destination,
      days: String(formData.days),
      currency: formData.currency,
    });

    if (formData.from && formData.from.trim()) {
      query.set("from", formData.from.trim());
    }

    // 🌍 Step 1: Fetch main trip plan
    const response = await fetch(`${import.meta.env.VITE_TRIP_SERVICE_URL}/api/travel/plan?${query.toString()}`);
    if (!response.ok) throw new Error("Failed to fetch travel plan");

    const data = await response.json();

    // 💰 Step 2: AI-powered cost estimation
    try {
      console.log("🔹 Calling AI cost estimation...");
      const costResponse = await fetch(`${import.meta.env.VITE_TRIP_SERVICE_URL}/api/travel/cost`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          from: formData.from || "",
          destination: formData.destination,
          days: formData.days,
          currency: formData.currency,
        }),
      });

      if (costResponse.ok) {
        const costData = await costResponse.json();
        console.log("AI Cost Response:", costData);

        if (costData.cost_estimate) {
          try {
            const match = costData.cost_estimate.match(/\[.*]/s);
            if(match){
              const parsed = JSON.parse(match[0]);
              if (Array.isArray(parsed)) {
              data.expenses = parsed; // ✅ Replace dummy expenses with AI-generated values
              }
            } else {
              console.warn("No JSON array found in AI response:", costData.cost_estimate);
            }
            
          } catch (e) {
            console.warn("Couldn't parse AI cost JSON:", e);
          }
        }
      } else {
        console.warn("AI cost API returned non-OK response");
      }
    } catch (aiErr) {
      console.error("AI cost fetch failed:", aiErr);
    }

    // ✅ Step 3: Update UI
    setPlan(data);
  } catch (err) {
    console.error("Error:", err);
    setError("Failed to fetch plan. Please try again later.");
  } finally {
    setLoading(false);
  }
};

  return (
    <div className="p-10 bg-gradient-to-b from-blue-50 to-blue-100 min-h-screen relative">

      {/* 🧭 Back to Dashboard */}
      <button
        onClick={() => navigate("/dashboard")}
        className="absolute top-5 left-5 bg-gray-700 text-white px-4 py-2 rounded-lg shadow-md hover:bg-gray-800 transition"
      >
        ⬅ Back to Dashboard
      </button>

      <h1 className="text-4xl font-bold text-center mb-10 text-blue-700">
        ✈ Trip Planner
      </h1>

      {/* Input Section */}
      <div className="bg-white p-8 rounded-2xl shadow-lg max-w-2xl mx-auto mb-10 border border-gray-200">
        <label className="block mb-4">
        <span className="text-gray-700 font-semibold">Starting City (optional)</span>
        <input
          type="text"
          placeholder="Where are you starting from? (e.g., Bengaluru)"
          value={formData.from}
          onChange={(e) => setFormData({ ...formData, from: e.target.value })}
          className="w-full mt-1 p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
        />
      </label>
        <label className="block mb-4">
          <span className="text-gray-700 font-semibold">Destination</span>
          <input
            type="text"
            placeholder="Enter your destination (e.g. Paris, Goa, Tokyo)..."
            value={formData.destination}
            onChange={(e) =>
              setFormData({ ...formData, destination: e.target.value })
            }
            className="w-full mt-1 p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
          />
        </label>

        <label className="block mb-4">
          <span className="text-gray-700 font-semibold">Number of Days</span>
          <input
            type="number"
            min="1"
            value={formData.days}
            onChange={(e) =>
              setFormData({ ...formData, days: e.target.value })
            }
            className="w-full mt-1 p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
          />
        </label>
        <label className="block mb-4">
            <span className="text-gray-700 font-semibold">Number of Travelers</span>
            <input
              type="number"
              min="1"
              value={formData.travelers || 1}
              onChange={(e) =>
                setFormData({ ...formData, travelers: e.target.value })
              }
              className="w-full mt-1 p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
              placeholder="Enter number of travelers"
            />
        </label>
        <label className="block mb-4">
          <span className="text-gray-700 font-semibold">Travel Mode</span>
          <select
            value={formData.travelMode}
            onChange={(e) =>
              setFormData({ ...formData, travelMode: e.target.value })
            }
            className="w-full mt-1 p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
          >
            <option value="Flight">✈ Flight</option>
            <option value="Train">🚆 Train</option>
            <option value="Bus">🚌 Bus</option>
            <option value="Car">🚗 Car</option>
          </select>
        </label>
        <label className="block mb-6">
          <span className="text-gray-700 font-semibold">Currency</span>
          <select
            value={formData.currency}
            onChange={(e) =>
              setFormData({ ...formData, currency: e.target.value })
            }
            className="w-full mt-1 p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
          >
            <option value="INR">₹ INR</option>
            <option value="USD">$ USD</option>
            <option value="EUR">€ EUR</option>
            <option value="GBP">£ GBP</option>
            <option value="JPY">¥ JPY</option>
          </select>
        </label>

        <button
          onClick={generatePlan}
          disabled={loading}
          className="w-full bg-blue-600 text-white p-3 rounded-lg hover:bg-blue-700 transition font-semibold text-lg"
        >
          {loading ? "Generating..." : "Generate My Plan 🚀"}
        </button>

        {error && <p className="text-red-600 mt-4 text-center">{error}</p>}
      </div>

      {/* Results Section */}
      {plan && (
        <div className="max-w-7xl mx-auto space-y-10">

          {/* 🌤 Weather Summary */}
          {plan.weather && <WeatherSummary weather={plan.weather} />}

          {/* 🌍 Trip Overview */}
          <div className="text-center mb-6">
            {/* <h2 className="text-2xl font-semibold text-gray-800">
              🌍 Total Distance:{" "}
              <span className="text-blue-600 font-bold">
                {plan.totalDistance} km
              </span>
            </h2> */}
            <h2 className="text-2xl font-semibold text-gray-800">
              🌍 Total Distance:{" "}
              <span className="text-blue-600 font-bold">
                {plan.totalDistance > 0
                  ? `${plan.totalDistance} km${plan.from ? ` from ${plan.from}` : ""}`
                  : "✈ Distance: N/A (please specify your starting city to get exact distance)"}
              </span>
            </h2>
          </div>

          <div className="grid md:grid-cols-2 gap-10">
            {/* Attractions + Restaurants */}
            <div className="bg-white p-8 rounded-xl shadow-md border border-gray-200">
              <h2 className="text-xl font-bold mb-4 text-blue-600">
                🏞 Top Attractions
              </h2>
              <ul className="list-disc pl-6 mb-6 text-gray-700 space-y-2">
                {plan.attractions?.length ? (
                  plan.attractions.map((p, i) => (
                    <li key={i}>
                      <strong>{p.name}</strong>
                      <div className="text-sm text-gray-500">{p.address}</div>
                    </li>
                  ))
                ) : (
                  <p>No attractions found.</p>
                )}
              </ul>

              <h2 className="text-xl font-bold mb-4 text-green-600">
                🍴 Restaurants
              </h2>
              <ul className="list-disc pl-6 text-gray-700 space-y-2">
                {plan.restaurants?.length ? (
                  plan.restaurants.map((r, i) => (
                    <li key={i}>
                      <strong>{r.name}</strong>
                      <div className="text-sm text-gray-500">{r.address}</div>
                    </li>
                  ))
                ) : (
                  <p>No restaurants found.</p>
                )}
              </ul>
            </div>

            {/* Expense Chart */}
            <div className="bg-white p-8 rounded-xl shadow-md flex flex-col items-center border border-gray-200">
              <h2 className="text-xl font-bold mb-4 text-purple-600">
                💰 Expense Breakdown
              </h2>
              <div className="w-full h-[350px]">
                <ResponsiveContainer>
                  <PieChart>
                    <Pie
                      data={plan.expenses}
                      dataKey="value"
                      nameKey="name"
                      cx="50%"
                      cy="50%"
                      outerRadius={130}
                      label={({ name, value }) =>
                        `${name}: ${currencySymbols[formData.currency] || ""}${value.toFixed(0)}`
                      }
                    >
                      {plan.expenses.map((_, i) => (
                        <Cell key={i} fill={COLORS[i % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip
                      formatter={(value) =>
                        `${currencySymbols[formData.currency] || ""}${value.toFixed(2)}`
                      }
                    />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>
          
          {/* 🌍 Interactive Map */}
            <div className="bg-white p-8 rounded-xl shadow-md border border-gray-200">
              <h2 className="text-xl font-bold mb-4 text-blue-700">
                🗺 Interactive Travel Map
              </h2>

              {(() => {
                const centerLat =
                  plan.attractions?.[0]?.lat ||
                  plan.restaurants?.[0]?.lat ||
                  20.5937;

                const centerLon =
                  plan.attractions?.[0]?.lon ||
                  plan.restaurants?.[0]?.lon ||
                  78.9629;

                return (
                  <div className="w-full h-[400px] rounded-xl overflow-hidden">
                    <MapContainer
                      center={[centerLat, centerLon]}
                      zoom={10}
                      style={{ height: "100%", width: "100%" }}
                    >
                      <TileLayer
                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        attribution="© OpenStreetMap contributors"
                      />

                      {/* 🔵 Attractions */}
                      {plan.attractions?.map((a, idx) =>
                        a.lat && a.lon ? (
                          <Marker key={idx} position={[a.lat, a.lon]}>
                            <Popup>
                              <strong>{a.name}</strong>
                              <br />
                              {a.address}
                            </Popup>
                          </Marker>
                        ) : null
                      )}

                      {/* 🟢 Restaurants */}
                      {plan.restaurants?.map((r, idx) =>
                        r.lat && r.lon ? (
                          <Marker key={idx} position={[r.lat, r.lon]}>
                            <Popup>
                              <strong>{r.name}</strong>
                              <br />
                              {r.address}
                            </Popup>
                          </Marker>
                        ) : null
                      )}
                    </MapContainer>
                  </div>
                );
              })()}
            </div>
          {/* Day-wise Plan */}
          <div className="bg-white p-8 rounded-xl shadow-md mt-10 border border-gray-200">
            <h2 className="text-2xl font-bold text-center mb-6 text-indigo-700">
              📅 Day-wise Trip Plan
            </h2>
            <div className="space-y-6">
              {plan.days?.length ? (
                plan.days.map((d, i) => (
                  <div
                    key={i}
                    className="border rounded-lg p-5 shadow-sm bg-gray-50 hover:shadow-md transition"
                  >
                    <h3 className="text-lg font-semibold mb-3 text-blue-700">
                      Day {d.day}
                    </h3>
                    <ul className="list-disc pl-6 text-gray-700 space-y-1">
                      {d.activities.map((a, j) => (
                        <li key={j}>{a}</li>
                      ))}
                    </ul>
                  </div>
                ))
              ) : (
                <p>No day plan generated.</p>
              )}
            </div>
          </div>

          {/* ✅ Save and Book Buttons */}
          <div className="text-center mt-10 space-x-4">
            {/* 💾 Save Trip */}
            {/* <button
              onClick={async () => {
                const user = JSON.parse(localStorage.getItem("user"));
                const userEmail = user?.email;
                const tripData = {
                  destination: formData.destination,
                  days: formData.days,
                  currency: formData.currency,
                  planData: JSON.stringify(plan),
                  status: "Planned",
                  userEmail,
                  startDate: new Date().toISOString().split("T")[0],
                  endDate: new Date(Date.now() + formData.days * 86400000)
                    .toISOString()
                    .split("T")[0],
                  createdAt: new Date().toISOString(),
                };
                tripData.totalCost = plan.expenses
                  ? plan.expenses.reduce((sum, e) => sum + e.value, 0)
                  : 0;

                try {
                  const res = await fetch(`{import.meta.env.VITE_TRIP_SERVICE_URL}/api/trip/save`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(tripData),
                  });

                  if (res.ok) {
                    const savedTrip = await res.json();
                    alert(`✅ Trip saved successfully (Trip ID: ${savedTrip.id})`);
                    setPlan({ ...plan, tripId: savedTrip.id });
                  } else {
                    alert("❌ Failed to save trip");
                  }
                } catch (err) {
                  console.error("Save failed:", err);
                  alert("❌ Something went wrong while saving the trip");
                }
              }}
              className="bg-green-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-green-700 transition"
            >
              💾 Save Trip
            </button> */}
            <button
              onClick={async () => {
                // const user = JSON.parse(localStorage.getItem("user"));
                const userEmail = localStorage.getItem("userEmail");

                if (!userEmail) {
                  alert("Please log in again. Email not found.");
                  return;
                }

                const tripData = {
                  destination: formData.destination,
                  days: formData.days,
                  currency: formData.currency,
                  planData: JSON.stringify(plan),
                  status: "Planned",
                  userEmail,
                  travelMode: formData.travelMode,
                  startDate: new Date().toISOString().split("T")[0],
                  endDate: new Date(Date.now() + formData.days * 86400000)
                    .toISOString()
                    .split("T")[0],
                  createdAt: new Date().toISOString(),
                };
                tripData.totalCost = plan.expenses
                  ? plan.expenses.reduce((sum, e) => sum + e.value, 0)
                  : 0;

                try {
                  const res = await fetch(`${import.meta.env.VITE_TRIP_SERVICE_URL}/api/trip/save`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(tripData),
                  });

                  if (res.ok) {
                    const savedTrip = await res.json();
                    alert(`✅ Trip saved successfully (Trip ID: ${savedTrip.id})`);
                    setPlan({ ...plan, tripId: savedTrip.id });
                  } else {
                    alert("❌ Failed to save trip");
                  }
                } catch (err) {
                  console.error("Save failed:", err);
                  alert("❌ Something went wrong while saving the trip");
                }
              }}
              className="bg-green-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-green-700 transition"
            >
              💾 Save Trip
            </button>

            {/* 🧳 Book Now */}
            {/* {plan?.tripId && (
              <button
                onClick={async () => {
                  try {
                    const res = await fetch(
                      `${import.meta.env.VITE_TRIP_SERVICE_URL}/api/trip/book/${plan.tripId}`,
                      { method: "PUT" }
                    );
                    if (res.ok) {
                      alert("🎉 Trip booked successfully!");
                      setPlan({ ...plan, status: "Booked" });
                    } else {
                      alert("❌ Booking failed, please try again.");
                    }
                  } catch (err) {
                    console.error("Booking failed:", err);
                    alert("⚠️ Error connecting to booking service");
                  }
                }}
                className="bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 transition"
              >
                🧳 Book Now
              </button>
            )} */}
            {plan?.tripId && (
  <button
    onClick={async () => {
      try {
        const res = await fetch(
          `${import.meta.env.VITE_TRIP_SERVICE_URL}/api/trip/book/${plan.tripId}`,
          { method: "PUT" }
        );

        if (res.ok) {
          const trip = await res.json();
          alert("🎉 Trip booked successfully! Redirecting to payment...");
          window.location.href = `/payment/${trip.id}`;
        } else {
          alert("❌ Booking failed, please try again.");
        }
      } catch (err) {
        console.error("Booking failed:", err);
        alert("⚠️ Error connecting to booking service");
      }
    }}
    className="bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 transition"
  >
    🧳 Book Now
  </button>
)}

          </div>
        </div>
      )}
    </div>
  );
}
