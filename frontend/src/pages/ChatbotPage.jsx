import { useState, useRef, useEffect } from 'react';
import { sendChatMessage } from '../api';
import SectionCard from '../components/SectionCard';
import { Send, Paperclip, Bot, User, Sparkles } from 'lucide-react';

const quickActions = [
  { label: 'Resume Tips', prompt: 'Give me tips to improve my resume' },
  { label: 'Job Search', prompt: 'Help me find jobs matching my skills' },
  { label: 'Interview Prep', prompt: 'Prepare me for a technical interview' },
  { label: 'Career Advice', prompt: 'What career path should I take?' }
];

export default function ChatbotPage() {
  const [sessionId, setSessionId] = useState(null);
  const [message, setMessage] = useState('');
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [attachedFile, setAttachedFile] = useState(null);
  const [showFileUpload, setShowFileUpload] = useState(false);
  const chatWindowRef = useRef(null);

  useEffect(() => {
    if (chatWindowRef.current) {
      chatWindowRef.current.scrollTop = chatWindowRef.current.scrollHeight;
    }
  }, [history]);

  const handleFileSelect = (file, error) => {
    if (error) {
      alert(`File error: ${error}`);
      return;
    }
    setAttachedFile(file);
    setShowFileUpload(false);
  };

  const submit = async (event) => {
    event.preventDefault();
    if (!message.trim() && !attachedFile) return;

    setLoading(true);
    try {
      const payload = {
        sessionId,
        message: message || (attachedFile ? `I've attached a file: ${attachedFile.name}` : ''),
        fileContext: attachedFile ? `File: ${attachedFile.name}` : null
      };

      const response = await sendChatMessage(payload);
      setSessionId(response.sessionId);
      setHistory(response.history);
      setMessage('');
      setAttachedFile(null);
    } catch (err) {
      alert('Failed to send message. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickAction = (prompt) => {
    setMessage(prompt);
  };

  return (
    <div className="page-grid">
      <SectionCard title="AI Career Assistant" subtitle="Chat for career guidance">
        <div className="chat-wrapper">
          <div className="chat-messages" ref={chatWindowRef}>
            {history.length === 0 ? (
              <div className="chat-empty">
                <div className="chat-empty-icon">
                  <Bot size={32} />
                </div>
                <p className="chat-empty-title">How can I help you today?</p>
                <p className="chat-empty-subtitle">Choose a topic or type your question</p>
                <div className="quick-actions">
                  {quickActions.map((action, idx) => (
                    <button
                      key={idx}
                      className="quick-action-btn"
                      onClick={() => handleQuickAction(action.prompt)}
                    >
                      <Sparkles size={14} />
                      {action.label}
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              <>
                {history.map((entry, index) => (
                  <div key={index} className={`chat-message ${entry.role}`}>
                    <div className="message-avatar">
                      {entry.role === 'user' ? <User size={16} /> : <Bot size={16} />}
                    </div>
                    <div className="message-content">
                      <p>{entry.content}</p>
                    </div>
                  </div>
                ))}
                {loading && (
                  <div className="chat-message assistant">
                    <div className="message-avatar">
                      <Bot size={16} />
                    </div>
                    <div className="message-content">
                      <div className="typing-dots">
                        <span></span><span></span><span></span>
                      </div>
                    </div>
                  </div>
                )}
              </>
            )}
          </div>

          <div className="chat-input-area">
            {attachedFile && (
              <div className="attached-file">
                <Paperclip size={14} />
                <span>{attachedFile.name}</span>
                <button type="button" onClick={() => setAttachedFile(null)}>×</button>
              </div>
            )}

            <form className="chat-form-compact" onSubmit={submit}>
              <input
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder="Ask about careers, jobs, or interviews..."
                disabled={loading}
              />
              <button
                type="button"
                className="btn-attach"
                onClick={() => setShowFileUpload(!showFileUpload)}
                title="Attach file"
              >
                <Paperclip size={18} />
              </button>
              <button type="submit" className="btn-send" disabled={loading || !message.trim()}>
                <Send size={18} />
              </button>
            </form>
          </div>
        </div>
      </SectionCard>
    </div>
  );
}
