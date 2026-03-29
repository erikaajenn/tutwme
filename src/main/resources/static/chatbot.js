const OWNER_EMAIL = 'your@email.com';
const OWNER_PHONE = '555-YOUR-NUMBER';

const responses = {
    greetings: {
        patterns: ['hello', 'hi', 'hey', 'good morning', 'good afternoon', 'howdy'],
        reply: `Hi there! Welcome to TutwMe. I can help you with:\n\n• Finding a tutor\n• Learning about subjects\n• Booking a session\n• General questions\n\nWhat can I help you with today?`
    },
    tutors: {
        patterns: ['tutor', 'tutors', 'who teaches', 'available tutors', 'find a tutor'],
        reply: `We currently have 3 tutors available:\n\n• Sarah Chen — Algebra, Calculus ($65/hr)\n• James Okafor — Biology, Chemistry, SAT Prep ($55/hr)\n• Maria Lopez — English Writing ($50/hr)\n\nVisit our home page to see full profiles and book a session!`
    },
    subjects: {
        patterns: ['subject', 'subjects', 'what do you teach', 'courses', 'classes'],
        reply: `We offer tutoring in:\n\n• Math — Algebra, Calculus\n• Science — Biology, Chemistry\n• English — Writing\n• Test Prep — SAT Prep\n\nWould you like to know which tutor teaches a specific subject?`
    },
    booking: {
        patterns: ['book', 'booking', 'schedule', 'appointment', 'session', 'reserve'],
        reply: `Booking a session is easy!\n\n1. Go to "Book a Session" in the menu\n2. Choose your tutor\n3. Pick a subject\n4. Select a date and time\n5. Click Confirm Booking\n\nWould you like me to take you there now?`,
        action: { label: 'Book a Session', url: 'booking.html' }
    },
    pricing: {
        patterns: ['price', 'cost', 'how much', 'rate', 'rates', 'pricing', 'fee', 'charge'],
        reply: `Our tutoring rates are:\n\n• Sarah Chen — $65/hr (Math)\n• James Okafor — $55/hr (Science & SAT)\n• Maria Lopez — $50/hr (English)\n\nSessions can be 30, 60, 90 or 120 minutes.`
    },
    location: {
        patterns: ['location', 'where', 'elk grove', 'sacramento', 'area', 'neighborhood', 'local'],
        reply: `TutwMe serves the Elk Grove and Sacramento community! We are a local tutoring service connecting neighbors with qualified tutors in your area.`
    },
    contact: {
        patterns: ['contact', 'reach', 'email', 'phone', 'talk to someone', 'human', 'person', 'help', 'support', 'customer service'],
        reply: `I'd be happy to connect you with our team!\n\nYou can reach us at:\n📧 ${OWNER_EMAIL}\n📞 ${OWNER_PHONE}\n\nWe typically respond within a few hours during business hours.`
    },
    login: {
        patterns: ['login', 'sign in', 'account', 'password', 'forgot', 'register', 'sign up'],
        reply: `You can log in or create an account from the Student Login page.\n\nIf you forgot your password, please contact us directly and we'll reset it for you.`,
        action: { label: 'Go to Login', url: 'login.html' }
    },
    sat: {
        patterns: ['sat', 'test prep', 'college', 'exam'],
        reply: `James Okafor specializes in SAT Prep at $55/hr. He has helped many students improve their scores significantly!\n\nWould you like to book a session with James?`,
        action: { label: 'Book with James', url: 'booking.html?tutor_id=2' }
    },
    thanks: {
        patterns: ['thank', 'thanks', 'thank you', 'awesome', 'great', 'perfect', 'helpful'],
        reply: `You're welcome! Is there anything else I can help you with? 😊`
    },
    bye: {
        patterns: ['bye', 'goodbye', 'see you', 'later', 'cya'],
        reply: `Goodbye! Feel free to come back anytime. Happy learning! 👋`
    }
};

const fallback = `I'm not sure about that one! Here's what I can help with:\n\n• Finding a tutor\n• Subjects and pricing\n• Booking a session\n• Contacting our team\n\nOr reach us directly at ${OWNER_EMAIL} for personal assistance.`;

function getResponse(input) {
    const lower = input.toLowerCase().trim();
    for (const key in responses) {
        const { patterns, reply, action } = responses[key];
        if (patterns.some(p => lower.includes(p))) {
            return { reply, action };
        }
    }
    return { reply: fallback };
}

function createChatbot() {
    const style = document.createElement('style');
    style.textContent = `
    #chat-bubble { position: fixed; bottom: 24px; right: 24px; width: 52px; height: 52px; background: #3498db; border-radius: 50%; display: flex; align-items: center; justify-content: center; cursor: pointer; z-index: 9999; box-shadow: 0 4px 12px rgba(0,0,0,0.2); }
    #chat-bubble svg { width: 24px; height: 24px; fill: white; }
    #chat-window { position: fixed; bottom: 88px; right: 24px; width: 320px; height: 440px; background: white; border-radius: 16px; box-shadow: 0 8px 32px rgba(0,0,0,0.15); display: none; flex-direction: column; z-index: 9999; overflow: hidden; }
    #chat-header { background: #2c3e50; color: white; padding: 14px 16px; display: flex; justify-content: space-between; align-items: center; }
    #chat-header h4 { font-size: 15px; font-weight: 500; font-family: sans-serif; }
    #chat-header span { font-size: 12px; color: #9FE1CB; font-family: sans-serif; }
    #chat-close { background: none; border: none; color: white; font-size: 20px; cursor: pointer; padding: 0; line-height: 1; }
    #chat-messages { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 10px; }
    .chat-msg { max-width: 85%; padding: 10px 14px; border-radius: 12px; font-size: 13px; line-height: 1.5; font-family: sans-serif; white-space: pre-wrap; }
    .chat-msg.bot { background: #f0f4f8; color: #333; align-self: flex-start; border-bottom-left-radius: 4px; }
    .chat-msg.user { background: #3498db; color: white; align-self: flex-end; border-bottom-right-radius: 4px; }
    .chat-action { align-self: flex-start; margin-top: -4px; }
    .chat-action a { display: inline-block; background: #3498db; color: white; font-size: 12px; padding: 6px 14px; border-radius: 20px; text-decoration: none; font-family: sans-serif; }
    .chat-action a:hover { background: #2980b9; }
    #chat-input-row { display: flex; padding: 10px 12px; border-top: 1px solid #eee; gap: 8px; }
    #chat-input { flex: 1; border: 1px solid #ddd; border-radius: 20px; padding: 8px 14px; font-size: 13px; outline: none; font-family: sans-serif; }
    #chat-input:focus { border-color: #3498db; }
    #chat-send { background: #3498db; color: white; border: none; border-radius: 50%; width: 34px; height: 34px; cursor: pointer; font-size: 16px; display: flex; align-items: center; justify-content: center; }
    #chat-send:hover { background: #2980b9; }
    #chat-badge { position: absolute; top: -4px; right: -4px; background: #e74c3c; color: white; border-radius: 50%; width: 18px; height: 18px; font-size: 11px; display: flex; align-items: center; justify-content: center; font-family: sans-serif; }
  `;
    document.head.appendChild(style);

    document.body.innerHTML += `
    <div id="chat-bubble" onclick="toggleChat()">
      <svg viewBox="0 0 24 24"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/></svg>
      <div id="chat-badge">1</div>
    </div>
    <div id="chat-window">
      <div id="chat-header">
        <div>
          <h4>TutwMe Support</h4>
          <span>● Online</span>
        </div>
        <button id="chat-close" onclick="toggleChat()">×</button>
      </div>
      <div id="chat-messages"></div>
      <div id="chat-input-row">
        <input id="chat-input" placeholder="Type a message..." onkeydown="if(event.key==='Enter') sendMessage()">
        <button id="chat-send" onclick="sendMessage()">➤</button>
      </div>
    </div>
  `;

    addBotMessage(`Hi! 👋 I'm the TutwMe assistant.\n\nI can help you find a tutor, learn about pricing, or book a session. What can I help you with?`);
}

function toggleChat() {
    const win = document.getElementById('chat-window');
    const badge = document.getElementById('chat-badge');
    const isOpen = win.style.display === 'flex';
    win.style.display = isOpen ? 'none' : 'flex';
    if (badge) badge.style.display = 'none';
}

function addBotMessage(text, action) {
    const messages = document.getElementById('chat-messages');
    const msg = document.createElement('div');
    msg.className = 'chat-msg bot';
    msg.textContent = text;
    messages.appendChild(msg);

    if (action) {
        const actionDiv = document.createElement('div');
        actionDiv.className = 'chat-action';
        actionDiv.innerHTML = `<a href="${action.url}">${action.label}</a>`;
        messages.appendChild(actionDiv);
    }

    messages.scrollTop = messages.scrollHeight;
}

function addUserMessage(text) {
    const messages = document.getElementById('chat-messages');
    const msg = document.createElement('div');
    msg.className = 'chat-msg user';
    msg.textContent = text;
    messages.appendChild(msg);
    messages.scrollTop = messages.scrollHeight;
}

function sendMessage() {
    const input = document.getElementById('chat-input');
    const text = input.value.trim();
    if (!text) return;
    input.value = '';
    addUserMessage(text);
    setTimeout(() => {
        const { reply, action } = getResponse(text);
        addBotMessage(reply, action);
    }, 400);
}

document.addEventListener('DOMContentLoaded', createChatbot);