import { useEffect } from "react";
import { useSearchParams } from "react-router-dom";

export default function PaymentStatus() {
  const [params] = useSearchParams();
  const orderId = params.get("order_id");
  const status = params.get("status");

  useEffect(() => {
    if (!orderId || !status) {
      window.location.href = "/payment/failure";
      return;
    }

    if (status === "PAID" || status === "SUCCESS") {
      window.location.href = `/payment/success?order_id=${orderId}`;
    } else {
      window.location.href = `/payment/failure?order_id=${orderId}`;
    }
  }, [orderId, status]);

  return <p>Processing payment status...</p>; // user won't even see this
}