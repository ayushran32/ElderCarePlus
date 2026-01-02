package com.eldercareplus.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri

data class Scheme(
    val title: String,
    val description: String,
    val details: String,
    val icon: ImageVector,
    val websiteUrl: String
)

@Composable
fun GovernmentSchemesScreen(navController: NavController) {
    val schemes = listOf(
        Scheme(
            title = "Indira Gandhi National Old Age Pension",
            description = "Monthly pension for senior citizens below poverty line",
            details = "₹200-500 per month for citizens aged 60+. Increased rates for 80+. Apply through local government office with age proof and income certificate.",
            icon = Icons.Default.AccountBalance,
            websiteUrl = "https://nsap.nic.in/"
        ),
        Scheme(
            title = "Pradhan Mantri Vaya Vandana Yojana",
            description = "Pension scheme providing assured returns",
            details = "Guaranteed pension for 10 years @ 7.4% p.a. Investment up to ₹15 lakh. Available for senior citizens aged 60+.",
            icon = Icons.Default.Savings,
            websiteUrl = "https://licindia.in/Products/Pension-Plans/Pradhan-Mantri-Vaya-Vandana-Yojana"
        ),
        Scheme(
            title = "Senior Citizen Savings Scheme",
            description = "Savings scheme with attractive interest rates",
            details = "Interest rate: 8.2% p.a. (Q1 FY 2024-25). Max deposit: ₹30 lakh. 5-year tenure. Available at post offices and banks.",
            icon = Icons.Default.AttachMoney,
            websiteUrl = "https://www.indiapost.gov.in/Financial/Pages/Content/Post-Office-Saving-Schemes.aspx"
        ),
        Scheme(
            title = "Ayushman Bharat - Health Insurance",
            description = "Free health insurance up to ₹5 lakh",
            details = "Covers hospitalization costs up to ₹5 lakh per family per year. Cashless treatment at empaneled hospitals. Apply with Aadhaar card.",
            icon = Icons.Default.HealthAndSafety,
            websiteUrl = "https://pmjay.gov.in/"
        ),
        Scheme(
            title = "Senior Citizen Rail Concession",
            description = "Discounts on railway tickets",
            details = "40% concession for men (60+) and women (58+) on all classes. 50% discount for men (60+) on Shatabdi, Rajdhani. ID proof required.",
            icon = Icons.Default.Train,
            websiteUrl = "https://www.indianrail.gov.in/enquiry/StaticPages/StaticContentDisplay.jsp?StaticContent=SrCitizenConcession.html"
        ),
        Scheme(
            title = "Income Tax Benefits",
            description = "Higher exemption limits for seniors",
            details = "Basic exemption: ₹3 lakh (60-80 years), ₹5 lakh (80+ years). Additional deductions under 80D for medical insurance. Section 80TTB for interest income.",
            icon = Icons.Default.Receipt,
            websiteUrl = "https://www.incometax.gov.in/iec/foportal/"
        )
    )
    
    var selectedScheme by remember { mutableStateOf<Scheme?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { 
                if (selectedScheme != null) {
                    selectedScheme = null
                } else {
                    navController.popBackStack()
                }
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                if (selectedScheme == null) "Government Schemes" else selectedScheme!!.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        if (selectedScheme == null) {
            // Scheme list
            Text(
                "Benefits and schemes for senior citizens",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                schemes.forEach { scheme ->
                    SchemeCard(scheme = scheme, onClick = { selectedScheme = scheme })
                    Spacer(Modifier.height(12.dp))
                }
            }
        } else {
            // Scheme details
            SchemeDetails(scheme = selectedScheme!!)
        }
    }
}

@Composable
fun SchemeCard(scheme: Scheme, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    scheme.icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    scheme.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    scheme.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun SchemeDetails(scheme: Scheme) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    scheme.icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    scheme.description,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    scheme.details,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color.DarkGray
                )
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Official Website Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scheme.websiteUrl))
                    context.startActivity(intent)
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.OpenInBrowser,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Visit Official Website",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF856404)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "How to Apply",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF856404)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Visit your nearest government office or bank with required documents. You can also apply online through official government portals.",
                        fontSize = 14.sp,
                        color = Color(0xFF856404)
                    )
                }
            }
        }
    }
}

