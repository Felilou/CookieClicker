// API Base URL
const API_BASE_URL = 'http://localhost:8081';

// LocalStorage Keys
const TOKEN_KEY = 'jwt_token';
const USERNAME_KEY = 'username';

/**
 * Login User
 */
async function login(username, password) {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            const error = await response.text();
            return {
                success: false,
                message: 'Login fehlgeschlagen. Bitte überprüfe deine Zugangsdaten.'
            };
        }

        const data = await response.json();

        // Speichere Token und Username
        localStorage.setItem(TOKEN_KEY, data.access_token);
        localStorage.setItem(USERNAME_KEY, username);

        return {
            success: true,
            token: data.access_token
        };
    } catch (error) {
        console.error('Login error:', error);
        return {
            success: false,
            message: 'Verbindungsfehler. Bitte versuche es später erneut.'
        };
    }
}

/**
 * Signup User
 */
async function signup(username, email, password, firstName, lastName) {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username,
                email,
                password,
                firstName: firstName || '',
                lastName: lastName || ''
            })
        });

        const data = await response.json();

        if (!response.ok) {
            return {
                success: false,
                message: data.message || 'Registrierung fehlgeschlagen.'
            };
        }

        return {
            success: true,
            message: data.message
        };
    } catch (error) {
        console.error('Signup error:', error);
        return {
            success: false,
            message: 'Verbindungsfehler. Bitte versuche es später erneut.'
        };
    }
}

/**
 * Logout User
 */
function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
}

/**
 * Check if user is authenticated
 */
function isAuthenticated() {
    const token = localStorage.getItem(TOKEN_KEY);
    return token !== null && token !== '';
}

/**
 * Get JWT Token
 */
function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

/**
 * Get Current Username
 */
function getCurrentUser() {
    return localStorage.getItem(USERNAME_KEY);
}

