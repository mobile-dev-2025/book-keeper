// Auth0 Integration Middleware
// This middleware is used to verify the Auth0 token from the Authorization header
// If the token is valid, it attaches the Auth0 user data to the request object'
const { verifyAuth0Token } = require('../utils/auth0.utils');

module.exports = async (req, res, next) => {
  try {
   // Verify Auth0 token from Authorization header
   const auth0Token = req.headers.authorization.split(' ')[1];
   const auth0User = await verifyAuth0Token(auth0Token); // Implement Auth0 token verification
   
   // Attach Auth0 user data to request
   req.auth0User = auth0User;
   next();
  } catch (error) {
    res.status(401).json({ error: 'Auth0 Authentication failed' });
  }
};
