const { JWTVerify } = require('@auth0/auth0-spa-js'); // Use server-side compatible method
const jwksClient = require('jwks-rsa');

// Set up connection to Auth0's key vault
const client = jwksClient({
  jwksUri: `https://${process.env.AUTH0_DOMAIN}/.well-known/jwks.json`
});

const verifyAuth0Token = async (token) => {
  try {
      // Full verification process
    const decoded = await JWTVerify(token, {
      audience: process.env.AUTH0_AUDIENCE,
      issuer: `https://${process.env.AUTH0_DOMAIN}/`,
      algorithms: ['RS256']
    });
    
    return decoded.payload;
  } catch (error) {
    console.error('Token verification failed:',{ error: error.message });
    throw new Error('Invalid token or expired token');
  }
};

module.exports = { verifyAuth0Token };
