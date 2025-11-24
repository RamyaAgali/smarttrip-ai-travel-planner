import React, { useState, useEffect, useRef } from "react";
import "./AIAssistant.css";

const AIAssistant = () => {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState(
    JSON.parse(localStorage.getItem("chatHistory")) || []
  );
  const [input, setInput] = useState("");
  const [typing, setTyping] = useState(false);
  const [userName, setUserName] = useState("");

  const messagesEndRef = useRef(null);

  //  Fetch logged-in user's name
  useEffect(() => {
    async function fetchLoggedInUser() {
      try {
        const token = localStorage.getItem("token");
        if (!token) return;

        const response = await fetch("http://localhost:8081/api/auth/profile", {
          method: "GET",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
          }
        });

        if (!response.ok) return;

        const data = await response.json();
        if (data.name) {
          const firstName = data.name.split(" ")[0];
          setUserName(firstName);
        }
      } catch (error) {
        console.log("Error fetching user:", error);
      }
    }

    fetchLoggedInUser();
  }, []);

  //  Auto-scroll
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    localStorage.setItem("chatHistory", JSON.stringify(messages));
  }, [messages]);

  //  Welcome Message
  useEffect(() => {
    if (open && messages.length === 0) {
      const name = userName || "there";
      setMessages([
        {
          sender: "bot",
          text: `Hi ${name} 👋 I'm your SmartTrip AI Assistant. How can I help you plan your trip today?`
        }
      ]);
    }
  }, [open, userName]);

  //  Send message
  const sendMessage = async () => {
    if (!input.trim()) return;

    const token = localStorage.getItem("token");

    const newUserMessage = { sender: "user", text: input };
    setMessages((prev) => [...prev, newUserMessage]);

    setInput("");
    setTyping(true);

    try {
      const res = await fetch("http://localhost:8086/api/ai/chat/message", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ message: input, 
          userName: localStorage.getItem("userEmail")
        })
      });

      const data = await res.json();
      setTyping(false);

      setMessages((prev) => [...prev, { sender: "bot", text: data.reply }]);
    } catch (err) {
      setTyping(false);
      setMessages((prev) => [
        ...prev,
        { sender: "bot", text: "Oops! Something went wrong. Try again." }
      ]);
    }
  };

  return (
    <div className="ai-container">
      {!open && (
        <button className="ai-floating-btn" onClick={() => setOpen(true)}>
          🤖
        </button>
      )}

      {open && (
        <div className="ai-box">
          <div className="ai-header">
            <span>SmartTrip Assistant 🌍</span>
            <button onClick={() => setOpen(false)}>✖</button>
          </div>

          <div className="ai-messages">
            {messages.map((msg, i) => (
              <div
                key={i}
                className={msg.sender === "user" ? "msg-user" : "msg-bot"}
              >
                {msg.text}
              </div>
            ))}

            {typing && (
              <div className="msg-bot typing">Assistant is typing...</div>
            )}

            <div ref={messagesEndRef} />
          </div>

          <div className="ai-input">
            <input
              value={input}
              placeholder="Ask your travel question..."
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && sendMessage()}
            />
            <button onClick={sendMessage}>Send</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default AIAssistant;