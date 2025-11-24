import React from "react";
import { Outlet } from "react-router-dom";
import AIAssistant from "./AIAssistant";

const ProtectedLayout = () => {
  return (
    <div className="relative min-h-screen">
      <Outlet />    {/* This shows the actual page content */}
      <AIAssistant />   {/* Chatbot appears automatically */}
    </div>
  );
};

export default ProtectedLayout;