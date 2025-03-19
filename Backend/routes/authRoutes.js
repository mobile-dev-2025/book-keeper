const express = require('express');
const router = express.Router();



// Check user existence and create if needed 
/**
 * Handles user initialization after Auth0 authentication
 * 1. Checks if user exists in application DB
 * 2. Creates new user record if not found
 * 3. Issues application-specific JWT
 */
router.post('/checkUser', async (req, res) => {
  const { sub } = req.sub; // From Auth0 token
  try {
    // Check existing user
    let user = await User.findOne({ sub });
    const isNewUser = !user;

      res.json({
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
  
module.exports = router;

