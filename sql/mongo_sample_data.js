db.prescriptions.insertMany([
  {
    patientName: "John Smith",
    appointmentId: 1,
    medication: "Vitamin C tablets",
    dosage: "Twice a day",
    doctorNotes: "NA",
    refillCount: 0,
    tags: ["otc", "supplement"]
  },
  {
    patientName: "Jane Doe",
    appointmentId: 3,
    medication: "Amoxicillin",
    dosage: "500mg, 3 times a day",
    doctorNotes: "Complete the full course.",
    refillCount: 1,
    pharmacy: { name: "Walgreens SF", location: "Market Street" },
    tags: ["antibiotic"]
  }
]);
