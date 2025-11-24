import { useNavigate, useLocation } from "react-router-dom";
import { motion } from "framer-motion";

export default function BackToDashboardButton() {
  const navigate = useNavigate();
  const location = useLocation();

  // Hide button if we're already on the dashboard
  if (location.pathname === "/dashboard") return null;

  return (
    <motion.button
      onClick={() => navigate("/dashboard")}
      initial={{ opacity: 0, y: -10 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -10 }}
      transition={{ duration: 0.3 }}
      className="fixed top-5 left-5 bg-white/90 text-blue-700 font-semibold px-5 py-2 rounded-full shadow-lg border border-blue-200 hover:bg-blue-50 hover:scale-105 transition-all duration-200 z-50"
    >
      ← Back to Dashboard
    </motion.button>
  );
}
