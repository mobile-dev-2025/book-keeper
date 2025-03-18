const express = require('express');
const router = express.Router();
const authMiddleware = require('../middlewares/auth');


// Check user existence and create if needed 
/**
 * Handles user initialization after Auth0 authentication
 * 1. Checks if user exists in application DB
 * 2. Creates new user record if not found
 * 3. Issues application-specific JWT
 */
router.post('/checkUser', authMiddleware, async (req, res) => {
  const { email } = req.auth0User; // From Auth0 token
  try {
    // Check existing user
    let user = await User.findOne({ email });
    const isNewUser = !user;

   // Create user if not found
    if (!user) {
      user = new User({ email, auth0Id: req.auth0User.sub });// Auth0's unique user identifier
      await user.save();
    }
     // Generate your own JWT for API access
     const token = jwt.sign(
      { userId: user._id,
        auth0Id: user.auth0Id 
       }, 
      process.env.JWT_SECRET,
      { expiresIn: '9h' }
    );

    res.json({
      token,
      userId: user._id,
      email: user.email,
      isNewUser: user.auth0Id ? isNewUser : undefined
    });
    
  } catch (error) {
    res.status(400).json({ error: 'User processing failed' });
    res.status(500).json({ 
      error: 'User processing failed',
      details: error.message 
    });
  }
});
  
// Refresh token
router.post('/refresh', authMiddleware, async (req, res) => {
  const { refreshToken } = req.body;
      //codes to be completed
      res.status(501).json({ error: 'Not implemented' });
    });

module.exports = router;

