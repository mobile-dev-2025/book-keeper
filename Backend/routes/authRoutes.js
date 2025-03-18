const express = require('express');
const router = express.Router();
const authMiddleware = require('../middlewares/auth');



// Check user existence and create if needed still incomplete though
router.post('/checkUser', authMiddleware, async (req, res) => {
  const { email } = req.auth0User;
  try {
    let user = await User.findOne({ email });
    if (!user) {
      // Create user if not found
      user = new User({ email, auth0Id: req.auth0User.sub });
      await user.save();
    }
     // Generate your own JWT for API access
     const token = jwt.sign(
      { userId: user._id }, 
      process.env.JWT_SECRET,
      { expiresIn: '1h' }
    );

    res.json({
      token,
      userId: user._id,
      email: user.email,
      isNewUser: !user.createdAt
    });
    
  } catch (error) {
    res.status(400).json({ error: 'User processing failed' });
  }
});
    res.send({ user, token });
 
router.post('/refresh', authMiddleware, async (req, res) => {
  const { refreshToken } = req.body;
      //codes to be completed
    });

module.exports = router;