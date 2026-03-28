import { useRef, useState } from 'react';
import { Upload, FileText, AlertCircle } from 'lucide-react';

export default function FileUpload({
  onFileSelect,
  accept = '.pdf,.doc,.docx',
  maxSizeMB = 10,
  loading = false,
  error = null,
  label = 'Upload File'
}) {
  const inputRef = useRef(null);
  const [dragActive, setDragActive] = useState(false);
  const [preview, setPreview] = useState(null);

  const handleFile = (file) => {
    if (!file) return;

    const maxBytes = maxSizeMB * 1024 * 1024;
    if (file.size > maxBytes) {
      onFileSelect(null, `File size must be less than ${maxSizeMB}MB`);
      return;
    }

    setPreview({
      name: file.name,
      size: (file.size / 1024).toFixed(2),
      type: file.type
    });

    onFileSelect(file, null);
  };

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFile(e.dataTransfer.files[0]);
    }
  };

  const handleChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      handleFile(e.target.files[0]);
    }
  };

  return (
    <div className="file-upload-container">
      <div
        className={`file-upload-zone ${dragActive ? 'drag-active' : ''} ${loading ? 'uploading' : ''}`}
        onDragEnter={handleDrag}
        onDragLeave={handleDrag}
        onDragOver={handleDrag}
        onDrop={handleDrop}
        onClick={() => inputRef.current?.click()}
        role="button"
        tabIndex={0}
        onKeyDown={(e) => e.key === 'Enter' && inputRef.current?.click()}
      >
        <input
          ref={inputRef}
          type="file"
          accept={accept}
          onChange={handleChange}
          style={{ display: 'none' }}
          disabled={loading}
        />

        {!preview && !loading && (
          <div className="upload-prompt">
            <Upload size={40} className="upload-icon" />
            <p className="upload-label">{label}</p>
            <p className="upload-hint">or drag and drop</p>
            <p className="upload-size">Max {maxSizeMB}MB</p>
          </div>
        )}

        {preview && !loading && (
          <div className="upload-preview">
            <FileText size={32} />
            <p className="preview-name">{preview.name}</p>
            <p className="preview-size">{preview.size} KB</p>
            <button
              type="button"
              className="ghost-button"
              onClick={(e) => {
                e.stopPropagation();
                setPreview(null);
                inputRef.current.value = '';
              }}
            >
              Choose different file
            </button>
          </div>
        )}

        {loading && (
          <div className="upload-loading">
            <div className="spinner"></div>
            <p>Processing file...</p>
          </div>
        )}
      </div>

      {error && (
        <div className="file-upload-error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}
    </div>
  );
}
