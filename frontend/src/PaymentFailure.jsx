import { useSearchParams } from "react-router-dom";

export default function PaymentFailure() {
  const [params] = useSearchParams();
  const orderId = params.get("order_id");
  const status = params.get("status") || "FAILED";

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-red-50">
      <div className="bg-white p-8 rounded-2xl shadow-lg text-center">
        <h1 className="text-3xl font-bold text-red-600 mb-4">Payment Failed 💔</h1>
        <p className="text-gray-700 mb-4">
          {status === "FAILED"
            `? Your payment for Order ID: ${orderId} was unsuccessful.
            : "Your payment was cancelled or declined."`}
        </p>
        <p className="text-gray-600 mb-4">
          If any amount was deducted, it will be refunded shortly.
        </p>
        <a
          href="/my-trips"
          className="bg-red-600 text-white px-5 py-3 rounded-lg hover:bg-red-700 transition"
        >
          Go Back to My Trips
        </a>
      </div>
    </div>
  );
}