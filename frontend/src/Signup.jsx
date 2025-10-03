// import { useState } from "react";

// export default function Signup() {
//   const [formData, setFormData] = useState({
//     name: "",
//     email: "",
//     password: "",
//     confirmPassword: "",
//   });

//   const handleChange = (e) => {
//     setFormData({
//       ...formData,
//       [e.target.name]: e.target.value,
//     });
//   };

//   const handleSubmit = async (e) => {
//     e.preventDefault();

//     if (formData.password !== formData.confirmPassword) {
//       alert("Passwords do not match!");
//       return;
//     }

//     try {
//       const response = await fetch("http://localhost:8081/api/auth/signup", {
//         method: "POST",
//         headers: { "Content-Type": "application/json" },
//         body: JSON.stringify({
//           name: formData.name,
//           email: formData.email,
//           password: formData.password,
//         }),
//       });

//       if (response.ok) {
//         alert("Signup successful! Please login.");
//         window.location.href = "/"; // ✅ redirect to login
//       } else {
//         alert("Signup failed.");
//       }
//     } catch (error) {
//       console.error("Error signing up:", error);
//       alert("Server error, please try again later.");
//     }
//   };

//   return (
//     <div className="flex h-screen">
//       {/* Left Branding */}
//       <div className="w-1/2 bg-blue-600 text-white flex flex-col justify-center items-center p-10">
//         <h1 className="text-4xl font-bold mb-4">SmartTrip</h1>
//         <p className="text-lg mb-6">Create your free account today</p>
//         <ul className="space-y-2 text-lg">
//           <li>✨ Plan smarter trips with AI</li>
//           <li>🗺️ Explore destinations on maps</li>
//           <li>🤝 Join travel communities</li>
//         </ul>
//       </div>

//       {/* Right Signup Form */}
//       <div className="w-1/2 flex justify-center items-center bg-white">
//         <div className="w-full max-w-md p-8">
//           <h2 className="text-2xl font-bold mb-6 text-gray-800">Sign Up for SmartTrip</h2>
//           <form className="space-y-4" onSubmit={handleSubmit}>
//             <div>
//               <label className="block text-gray-600">Name</label>
//               <input
//                 type="text"
//                 name="name"
//                 value={formData.name}
//                 onChange={handleChange}
//                 placeholder="Your Name"
//                 className="w-full p-3 mt-1 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
//               />
//             </div>
//             <div>
//               <label className="block text-gray-600">Email</label>
//               <input
//                 type="email"
//                 name="email"
//                 value={formData.email}
//                 onChange={handleChange}
//                 placeholder="you@example.com"
//                 className="w-full p-3 mt-1 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
//               />
//             </div>
//             <div>
//               <label className="block text-gray-600">Password</label>
//               <input
//                 type="password"
//                 name="password"
//                 value={formData.password}
//                 onChange={handleChange}
//                 placeholder="••••••••"
//                 className="w-full p-3 mt-1 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
//               />
//             </div>
//             <div>
//               <label className="block text-gray-600">Confirm Password</label>
//               <input
//                 type="password"
//                 name="confirmPassword"
//                 value={formData.confirmPassword}
//                 onChange={handleChange}
//                 placeholder="••••••••"
//                 className="w-full p-3 mt-1 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
//               />
//             </div>
//             <button
//               type="submit"
//               className="w-full bg-blue-600 text-white p-3 rounded-lg hover:bg-blue-700 transition"
//             >
//               Sign Up
//             </button>
//             <p className="text-center text-gray-500 mt-4">
//               Already have an account? <a href="/" className="text-blue-600">Login</a>
//             </p>
//           </form>
//         </div>
//       </div>
//     </div>
//   );
// }
import { useState } from "react";
import { getPasswordStrength } from "./utils/passwordStrenth";

export default function Signup() {
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
  });

  const [strength, setStrength] = useState(null);
  const [passwordMatch, setPasswordMatch] = useState(true);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });

    if (name === "password") {
      setStrength(getPasswordStrength(value));
      setPasswordMatch(value === formData.confirmPassword);
    }
    if (name === "confirmPassword") {
      setPasswordMatch(value === formData.password);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!passwordMatch) {
      alert("Passwords do not match!");
      return;
    }

    try {
      const response = await fetch("http://localhost:8081/api/auth/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: formData.name,
          email: formData.email,
          password: formData.password,
        }),
      });

      if (response.ok) {
        alert("Signup successful! Please login.");
        window.location.href = "/";
      } else {
        alert("Signup failed.");
      }
    } catch (error) {
      console.error("Error signing up:", error);
      alert("Server error, please try again later.");
    }
  };

  return (
    <div className="flex h-screen">
      {/* Left Branding */}
      <div className="w-1/2 bg-blue-600 text-white flex flex-col justify-center items-center p-10">
        <h1 className="text-4xl font-bold mb-4">SmartTrip</h1>
        <p className="text-lg mb-6">Create your free account today</p>
        <ul className="space-y-2 text-lg">
          <li>✨ Plan smarter trips with AI</li>
          <li>🗺️ Explore destinations on maps</li>
          <li>🤝 Join travel communities</li>
        </ul>
      </div>

      {/* Right Signup Form */}
      <div className="w-1/2 flex justify-center items-center bg-white">
        <div className="w-full max-w-md p-8">
          <h2 className="text-2xl font-bold mb-6 text-gray-800">
            Sign Up for SmartTrip
          </h2>
          <form className="space-y-4" onSubmit={handleSubmit}>
            {/* Name */}
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="Your Name"
              className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
            />

            {/* Email */}
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="you@example.com"
              className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
            />

            {/* Password */}
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="••••••••"
              className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
            />

            {/* Strength Meter */}
            {strength && (
              <div>
                <div className="w-full bg-gray-200 h-2 rounded-lg">
                  <div className={`h-2 rounded-lg ${strength.color}`}></div>
                </div>
                <p className="text-sm mt-1 text-gray-600">{strength.label}</p>
              </div>
            )}

            {/* Confirm Password */}
            <input
              type="password"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              placeholder="Confirm Password"
              className={`w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500 ${
                !passwordMatch ? "border-red-500" : ""
              }`}
            />
            {!passwordMatch && (
              <p className="text-red-500 text-sm mt-1">Passwords do not match</p>
            )}

            <button
              type="submit"
              className="w-full bg-blue-600 text-white p-3 rounded-lg hover:bg-blue-700 transition"
            >
              Sign Up
            </button>
            <p className="text-center text-gray-500 mt-4">
              Already have an account?{" "}
              <a href="/" className="text-blue-600">
                Login
              </a>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
}
