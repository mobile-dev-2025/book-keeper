const express = require('express');
const router = express.Router();


// Auth0 Integration Middleware
const auth0Authentication = async (req, res, next) => {
  try {
    // Verify Auth0 token from Authorization header
    const auth0Token = req.headers.authorization.split(' ')[1];
    const auth0User = await verifyAuth0Token(auth0Token); // Implement Auth0 token verification
    
    // Attach Auth0 user data to request
    req.auth0User = auth0User;
    next();
  } catch (error) {
    res.status(401).json({ error: 'Auth0 authentication failed' });
  }
};

// Check user existence and create if needed still incomplete though
router.post('/checkUser', auth0Authentication, async (req, res) => {
  const { email } = req.auth0User;
  try {
    let user = await User.findOne({ email });
    if (!user) {
      // Create user if not found
      user = new User({ email, auth0Id: req.auth0User.sub });
      await user.save();
    }
    const token = jwt.sign({ userId: user._id }, process.env.JWT_SECRET);
    res.send({ user, token });
  } catch (error) {
    res.status(400).send(error);
  }
});

module.exports = router;