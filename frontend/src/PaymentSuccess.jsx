import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";

export default function PaymentSuccess() {
  const [params] = useSearchParams();
  const [message, setMessage] = useState("Processing your payment...");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const confirmPayment = async () => {
      try {
        // 🔹 Read query params returned by Cashfree
        const orderId = params.get("order_id");
        const paymentId = params.get("payment_id");
        const status = params.get("status") || "SUCCESS";
        const method = params.get("payment_mode") || "CARD";

        if (!orderId || !paymentId) {
          setMessage("❌ Invalid payment details. Please contact support.");
          setLoading(false);
          return;
        }

        // 🔹 Call backend to finalize & send invoice email
        const res = await fetch("http://localhost:8084/api/payment/success", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ orderId, paymentId, method }),
        });

        if (!res.ok) {
          const txt = await res.text();
          throw new Error(`Server error: ${txt}`);
        }

        const data = await res.json();
        console.log("✅ Payment confirmed:", data);

        setMessage(
          "✅ Payment successful! Your booking is confirmed and an invoice has been emailed to you."
        );
        setTimeout(() => {
          window.location.href = "/my-trips";
        }, 4000);
      } catch (err) {
        console.error("Payment confirmation failed:", err);
        setMessage("❌ Something went wrong. Please contact support.");
      } finally {
        setLoading(false);
      }
    };

    confirmPayment();
  }, [params]);

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gradient-to-b from-green-50 to-green-100">
      <div className="bg-white p-8 rounded-2xl shadow-lg max-w-md text-center">
        <h1 className="text-3xl font-bold text-green-700 mb-4">SmartTrip ✈</h1>
        {loading ? (
          <p className="text-gray-600 animate-pulse">{message}</p>
        ) : (
          <>
            <p className="text-gray-700 mb-4">{message}</p>
            <a
              href="/my-trips"
              className="bg-green-600 text-white px-5 py-3 rounded-lg hover:bg-green-700 transition"
            >
              View My Trips
            </a>
          </>
        )}
      </div>
    </div>
  );
}