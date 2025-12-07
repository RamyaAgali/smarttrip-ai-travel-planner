import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

function PaymentPage() {
  const { tripId } = useParams(); // ✅ Get trip ID from URL
  const [trip, setTrip] = useState(null);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // ✅ Fetch logged-in user profile
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      setError("Please login to proceed with payment.");
      setLoading(false);
      return;
    }

    fetch(`${import.meta.env.VITE_AUTH_SERVICE_URL}/api/auth/profile`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then((data) => {
        if (data.error) {
          setError("Session expired. Please log in again.");
        } else {
          setUser(data);
        }
        setLoading(false);
      })
      .catch((err) => {
        console.error("Profile fetch failed:", err);
        setError("Could not fetch user details.");
        setLoading(false);
      });
  }, []);

  // ✅ Fetch trip details using tripId
  useEffect(() => {
    if (!tripId) return;
    fetch(`${import.meta.env.VITE_TRIP_SERVICE_URL}/api/trip/${tripId}`)
      .then((res) => {
        if (!res.ok) throw new Error("Failed to fetch trip details");
        return res.json();
      })
      .then((data) => setTrip(data))
      .catch((err) => {
        console.error("Failed to load trip details:", err);
        setError("Unable to load trip details. Please try again.");
      });
  }, [tripId]);

  const handlePayment = async () => {
    if (!user) {
      alert("User not loaded yet.");
      return;
    }
    if (!trip) {
      alert("Trip not loaded yet.");
      return;
    }

    try {
      const payload = {
        tripId: trip.id,
        amount: trip.totalCost,
        currency: trip.currency || "INR",
        email: user.email, // ✅ real logged-in email
        
      };

      console.log("💰 Sending payment payload:", payload);

      const res = await fetch(`${import.meta.env.VITE_TRIP_SERVICE_URL}/api/payment/create`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const errData = await res.text();
        throw new Error(Create `payment failed: ${errData}`);
      }

      const data = await res.json();
      console.log("✅ Payment response:", data);

      // ✅ Redirect to Cashfree payment page if available
      // const paymentLink =
      //   data.cashfreeResponse?.payment_link ||
      //   data.cashfreeResponse?.payment_link_url ||
      //   data.cashfreeResponse?.payments?.url;
      const paymentLink = data.cashfreeResponse?.payment_session_id ?
      ` https://sandbox.cashfree.com/pg/checkout?payment_session_id=${data.cashfreeResponse.payment_session_id}`
      : null;

      if (paymentLink) {
        window.location.href = paymentLink;
        //Fallback: after 45 seconds, redirect to result page if user stuck
        setTimeout(() => {
          if(!window.location.href.includes("cashfree.com"))
          {
            window.location.href = "http://localhost:5173/payment/failure";
          }
        }, 30000);
      } else {
        alert("Order created successfully but no redirect link found.");
      }
    } catch (err) {
      console.error("Payment error:", err);
      setError(err.message);
    }
  };

  // ✅ Handle loading & errors gracefully
  if (loading) return <p>Loading user details...</p>;
  if (error) return <p className="text-red-600">{error}</p>;
  if (loading || !trip)
    return <p className="text-center text-gray-600">Loading trip details...</p>;

  // ✅ Main UI
  return (
    <div className="p-6 max-w-xl mx-auto bg-white shadow-md rounded-lg mt-10">
      <h2 className="text-2xl font-semibold mb-4">Payment for Trip</h2>

      <div className="mb-4">
        <p>
          <strong>User:</strong> {user.name} ({user.email})
        </p>
        <p>
          <strong>Destination:</strong> {trip.destination}
        </p>
        <p>
          <strong>Amount:</strong> ₹{trip.totalCost} {trip.currency}
        </p>
      </div>

      <button
        onClick={handlePayment}
        className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700 transition"
      >
        Proceed to Pay
      </button>

      {error && <p className="mt-4 text-red-500">{error}</p>}
    </div>
  );
}

export default PaymentPage;