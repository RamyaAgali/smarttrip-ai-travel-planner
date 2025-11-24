import { useEffect, useState } from "react";
import BackToDashboardButton from "./components/BackToDashboardButton";

export default function MyTrips() {
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  //const user = JSON.parse(localStorage.getItem("user"));
  const userEmail = localStorage.getItem("userEmail")// Replace with logged-in user's email
  

  const fetchTrips = async () => {
    try {
      const res = await fetch(`http://localhost:8084/api/trip/user/${userEmail}`);
      const data = await res.json();
      setTrips(data);
    } catch (err) {
      console.error("Failed to fetch trips:", err);
    } finally {
      setLoading(false);
    }
  };

  const cancelTrip = async (id) => {
    if (!window.confirm("Are you sure you want to cancel this trip?")) return;
    try {
      const res = await fetch(`http://localhost:8084/api/trip/cancel/${id}`, { method: "PUT" });
      if (res.ok) {
        alert("Trip cancelled successfully!");
        fetchTrips();
      } else {
        alert("Failed to cancel trip");
      }
    } catch (err) {
      console.error("Cancel error:", err);
    }
  };

  const rebookTrip = async (id) => {
    try {
      const res = await fetch(`http://localhost:8084/api/trip/rebook/${id}`, {
        method: "PUT",
      });
      if (res.ok) {
        alert("🎉 Trip rebooked successfully!");
        fetchTrips(); // Refresh list
      } else {
        alert("Failed to rebook trip");
      }
    } catch (err) {
      console.error("Rebook error:", err);
    }
  };
      // 💳 Proceed to payment (Cashfree Sandbox Integration)
const handlePayment = async (trip) => {
  try {
    // 1️⃣ Create order in backend (calls Cashfree API)
    const res = await fetch("http://localhost:8084/api/payment/create", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        tripId: trip.id,
        amount: trip.totalCost || 500,
        currency: trip.currency || "INR",
        //email: userEmail,
        email: userEmail.replace("@", "_").replace(".","__"),
      }),
    });

    if (!res.ok) throw new Error("❌ Failed to create payment order");
    const data = await res.json();
    console.log("✅ Cashfree order created:", data);

    // 2️⃣ Get Cashfree payment link (from sandbox response)
    // const paymentLink =
    //   data?.cashfreeResponse?.payment_link ||
    //   data?.cashfreeResponse?.payment_link_url ||
    //   data?.cashfreeResponse?.payment_session_id
    //     ? `https://sandbox.cashfree.com/pgapp/v1/checkout.js?payment_session_id=${data.cashfreeResponse.payment_session_id}`
    //     : null;
      const paymentLink =
      data?.cashfreeResponse?.payment_link ||
      data?.cashfreeResponse?.payment_link_url ||
      (data?.cashfreeResponse?.payment_session_id
      ? `https://sandbox.cashfree.com/pg/checkout?payment_session_id=${data.cashfreeResponse.payment_session_id}`
      : null);

    if (!paymentLink) {
      console.error("❌ Invalid Cashfree response:", data);
      alert("Could not initiate payment — please try again.");
      return;
    }

    // // 3️⃣ Redirect user to Cashfree’s payment page
    // window.open(paymentLink, "_blank");

    // // 4️⃣ Simulate success confirmation after redirection
    // setTimeout(async () => {
    //   await fetch("http://localhost:8084/api/payment/success", {
    //     method: "POST",
    //     headers: { "Content-Type": "application/json" },
    //     body: JSON.stringify({
    //       orderId: data.orderId,
    //       paymentId: "PAY-" + Date.now(),
    //       method: "CARD",
    //     }),
    //   });
    //   alert("✅ Payment successful! Invoice will be sent to your email.");
    //   fetchTrips();
    // }, 5000);
    // 3️⃣ Open Cashfree Checkout using SDK
    if (data.cashfreeResponse?.payment_session_id) {
      const paymentSessionId = data.cashfreeResponse.payment_session_id;

      // 🧠 Cashfree SDK from script tag (must be loaded in index.html)
      const cashfree = window.Cashfree({
        mode: "sandbox", // Use "production" later when live
      });

      // 💳 Launch the real Cashfree checkout flow
      cashfree.checkout({
        paymentSessionId: paymentSessionId,
        redirectTarget: "_self", // open in same tab
      });

      console.log("✅ Redirecting to Cashfree Checkout...");
    } else {
      alert("⚠ Failed to load Cashfree checkout page.");
      console.error("Cashfree response:", data);
    }
  } catch (err) {
    console.error("Payment error:", err);
    alert("⚠ Payment initialization failed.");
  }
};
  useEffect(() => {
    fetchTrips();
  }, []);

  if (loading)
    return <p className="text-center text-gray-500 mt-20">Loading your trips...</p>;

  return (
    <div className="p-10 bg-gradient-to-b from-blue-50 to-blue-100 min-h-screen overflow-y-auto max-h-screen">
      {/* ✅ Back to Dashboard Button */}
      <div className="mb-6">
        <BackToDashboardButton />
      </div>

      <h1 className="text-3xl font-bold text-center mb-10 text-blue-700">🌍 My Trips</h1>

      {trips.length === 0 ? (
        <p className="text-center text-gray-600">No trips found yet.</p>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
          {trips.map((trip) => (
            <div
              key={trip.id}
              className="bg-white shadow-md p-6 rounded-xl border border-gray-200 hover:shadow-lg transition"
            >
              <h2 className="text-xl font-bold text-blue-700">{trip.destination}</h2>
              <p className="text-gray-600 mt-1">
                🗓 {trip.startDate} → {trip.endDate}
              </p>
              <p className="mt-2 text-gray-800">
                💰 {trip.currency} {trip.totalCost?.toFixed(2) || "0.00"}
              </p>
              <p
                className={`mt-2 font-semibold ${
                  trip.status === "Booked"
                    ? "text-green-600"
                    : trip.status === "Paid"
                    ? "text-purple-600"
                    : trip.status === "Cancelled"
                    ? "text-red-600"
                    : "text-gray-600"
                }`}
              >
                {trip.status}
              </p>
              <p className="mt-2 text-gray-700">
                🚗 Travel Mode: <strong>{trip.travelMode || "Not specified"}</strong>
              </p>
              <div className="mt-4 flex flex-col gap-2 text-center">
                {trip.status === "Booked" && (
                  <button
                    onClick={() => handlePayment(trip)}
                    className="bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700 transition"
                  >
                    💳 Proceed to Payment
                  </button>
                )}

                {trip.status !== "Cancelled" ? (
                  <button
                    onClick={() => cancelTrip(trip.id)}
                    className="bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition"
                  >
                    Cancel
                  </button>
                ) : (
                  <div>
                    <p className="text-yellow-600 text-sm">
                      ⏳ You can rebook this trip within 24 hours
                    </p>
                    <button
                      onClick={() => rebookTrip(trip.id)}
                      className="mt-2 bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600 transition"
                    >
                      🔁 Rebook
                    </button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
