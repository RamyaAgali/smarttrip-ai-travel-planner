import { useState } from "react";
export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!email) {
      setMessage("⚠️ Please enter your email");
      return;
    }

    try {
      const response = await fetch("http://localhost:8081/api/auth/forgot-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
      });

      if (response.ok) {
        setMessage("✅ Reset link sent! Please check your email.");
      } else {
        setMessage("❌ No account found with this email.");
      }
    } catch (error) {
      console.error("Error:", error);
      setMessage("❌ Server error, try again later.");
    }
  };

  return (
    <div className="flex h-screen">
      {/* Left Branding */}
      <div className="w-1/2 bg-blue-600 text-white flex flex-col justify-center items-center p-10">
        <h1 className="text-4xl font-bold mb-4">SmartTrip</h1>
        <p className="text-lg mb-6">Reset your password securely</p>
        <ul className="space-y-2 text-lg">
          <li>📧 Enter your registered email</li>
          <li>🔗 Receive a reset link</li>
          <li>🔑 Create a new password</li>
        </ul>
      </div>

      {/* Right Forgot Password Form */}
      <div className="w-1/2 flex justify-center items-center bg-white">
        <div className="w-full max-w-md p-8">
          <h2 className="text-2xl font-bold mb-6 text-gray-800 text-center">
            Forgot Password?
          </h2>
          <form className="space-y-4" onSubmit={handleSubmit}>
            <div>
              <label className="block text-gray-600">Email</label>
              <input
                type="email"
                name="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                className="w-full p-3 mt-1 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <button
              type="submit"
              className="w-full bg-blue-600 text-white p-3 rounded-lg hover:bg-blue-700 transition"
            >
              Send Reset Link
            </button>
            {message && (
              <p className="text-center mt-4 text-gray-700">{message}</p>
            )}
            <p className="text-center text-gray-500 mt-4">
              <a href="/" className="text-blue-600">Back to Login</a>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
}
