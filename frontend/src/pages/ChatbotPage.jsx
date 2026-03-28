import { useState, useRef, useEffect } from 'react';
import { sendChatMessage } from '../api';
import SectionCard from '../components/SectionCard';
import FileUpload from '../components/FileUpload';
import { Send, Paperclip } from 'lucide-react';

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

  return (
    <div className="applicant-home">
      <div className="hero-panel">
        <div>
          <p className="eyebrow accent-eyebrow">AI Career Assistant</p>
          <h2>Career Chatbot</h2>
          <p className="hero-copy">
            Ask questions about resume improvements, job recommendations, interview preparation, and career guidance. Our AI assistant is here to help!
          </p>
        </div>
        <div className="hero-side-card">
          <strong>Chat Features</strong>
          <div className="feature-mini">
            <span>💬 Context-aware</span>
            <span>📎 File upload</span>
            <span>💡 Smart suggestions</span>
            <span>📝 Persistent history</span>
          </div>
        </div>
      </div>

      <SectionCard title="Chat Window" subtitle="Real-time conversation with AI">
        <div className="chat-container">
          <div className="chat-window" ref={chatWindowRef}>
            {history.length === 0 && (
              <div className="empty-chat">
                <p className="chat-welcome">👋 Hi! I'm your career assistant.</p>
                <p className="chat-hint">Ask me about resume tips, job preparation, or career guidance.</p>
              </div>
            )}
            {history.map((entry, index) => (
              <div key={`${entry.role}-${index}`} className={`chat-bubble ${entry.role}`}>
                {entry.role === 'user' ? '👤 You' : '🤖 Assistant'}
                <p>{entry.content}</p>
              </div>
            ))}
            {loading && (
              <div className="chat-bubble assistant">
                <p className="typing-indicator">
                  <span></span><span></span><span></span>
                </p>
              </div>
            )}
          </div>

          <div className="chat-input-section">
            {attachedFile && (
              <div className="attached-file-badge">
                <Paperclip size={16} />
                <span>{attachedFile.name}</span>
                <button
                  type="button"
                  onClick={() => setAttachedFile(null)}
                  className="badge-close"
                >
                  ✕
                </button>
              </div>
            )}

            {showFileUpload && (
              <div className="file-upload-mini">
                <FileUpload
                  onFileSelect={handleFileSelect}
                  accept=".pdf,.doc,.docx,.txt"
                  maxSizeMB={10}
                  label="Attach Document"
                />
              </div>
            )}

            <form className="form inline-form chat-form" onSubmit={submit}>
              <input
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder="Type your question here..."
                disabled={loading}
              />
              <button
                type="button"
                className="ghost-button"
                onClick={() => setShowFileUpload(!showFileUpload)}
                title="Attach file"
                disabled={loading}
              >
                <Paperclip size={18} />
              </button>
              <button type="submit" disabled={loading || (!message.trim() && !attachedFile)}>
                <Send size={18} />
              </button>
            </form>
          </div>
        </div>
      </SectionCard>
    </div>
  );
}
