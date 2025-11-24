// import { useEffect } from "react";

// export default function PaymentFailure() {
//   useEffect(() => {
//     const timer = setTimeout(() => {
//       window.location.href = "/my-trips";
//     }, 5000); // redirects after 5 seconds

//     return () => clearTimeout(timer);
//   }, []);

//   return (
//     <div className="flex flex-col items-center justify-center min-h-screen bg-red-50">
//       <div className="bg-white p-8 rounded-2xl shadow-lg text-center">
//         <h1 className="text-3xl font-bold text-red-600 mb-4">Payment Failed 💔</h1>
//         <p className="text-gray-700 mb-4">
//           Your payment could not be completed. If any amount was deducted, it will be refunded shortly.
//         </p>
//         <p className="text-gray-500 mb-2">Redirecting to My Trips in 5 seconds...</p>
//         <a
//           href="/my-trips"
//           className="bg-red-600 text-white px-5 py-3 rounded-lg hover:bg-red-700 transition"
//         >
//           Go Back to My Trips
//         </a>
//       </div>
//     </div>
//   );
// }
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