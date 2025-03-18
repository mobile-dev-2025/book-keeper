const { verifyAuth0Token } = require('../utils/auth0.utils');
// Auth0 Integration Middleware
// This middleware is used to verify the Auth0 token from the Authorization header
// If the token is valid, it attaches the Auth0 user data to the request object


module.exports = async (req, res, next) => {
  try {
   // Validate authorization header format
   if (!req.headers.authorization?.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Missing authorization token' });
  } 
   // Verify Auth0 token from Authorization header
   const auth0Token = req.headers.authorization.split(' ')[1];
   const auth0User = await verifyAuth0Token(auth0Token); // Implement Auth0 token verification
   
   // Attach Auth0 user data to request
   req.auth0User = auth0User;
   next(); // Continue to the next middleware
  } catch (error) {
    console.error('Auth0 authentication error:', error);
    res.status(401).json({ 
      error: 'Authentication failed',
      details: error.message 
    });
}
}
